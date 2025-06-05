package com.chinmay.GPSService1.listener; // New package for listeners

import com.chinmay.GPSService1.ExtendedGpsInput;    // Your DTO
import com.chinmay.GPSService1.config.RabbitMQConfig; // Your RabbitMQ constants
import com.chinmay.GPSService1.service.GpsService;   // Your existing service
import com.fasterxml.jackson.databind.ObjectMapper;  // For JSON deserialization
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener; // Key annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // Marks this as a Spring-managed component so @RabbitListener is detected
@Slf4j
public class GpsDataMessageListener {

    private final GpsService gpsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GpsDataMessageListener(GpsService gpsService, ObjectMapper objectMapper) {
        this.gpsService = gpsService;
        this.objectMapper = objectMapper;
    }

    // This method will be automatically invoked when a message arrives
    // on the queue specified by RabbitMQConfig.GPS_DATA_QUEUE_NAME
    @RabbitListener(queues = RabbitMQConfig.GPS_DATA_QUEUE_NAME)
    public void handleIncomingGpsData(String messagePayload) { // Receives the message body as a String
        log.info("Listener: Received message from queue '{}'. Payload='{}'",
                RabbitMQConfig.GPS_DATA_QUEUE_NAME, messagePayload);

        try {
            // Deserialize the JSON string payload back into our ExtendedGPSInput DTO
            ExtendedGpsInput gpsInput = objectMapper.readValue(messagePayload, ExtendedGpsInput.class);

            log.info("Listener: Successfully deserialized message for publisher '{}'. Attempting to save data.",
                    gpsInput.getPublisherId());

            // Now, call the existing GpsService to perform validation and save to the database.
            // This reuses all your existing business logic for saving.
            gpsService.saveGpsData(gpsInput); // This will go through your service's validation and DB save

            log.info("Listener: Successfully processed and saved GPS data from queue for publisher '{}'.",
                    gpsInput.getPublisherId());

        } catch (IllegalArgumentException e) {
            // This error likely came from GpsService validation (e.g., missing fields, bad format after deserialization).
            // The message is probably "bad" and might not be processable.
            log.error("Listener: Validation error while processing GPS data from queue. Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            // Implement Dead Letter Queue (DLQ) handling strategy.
            throw new AmqpRejectAndDontRequeueException("Validation failed: " + e.getMessage()); // Example for DLQ
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Listener: Error deserializing message from queue (malformed JSON). Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            // Ensure this line is NOT commented out and is definitely being executed
            throw new AmqpRejectAndDontRequeueException("Malformed JSON message", e);
        } catch (Exception e) {
            log.error("Listener: Unexpected error processing GPS data message from queue. Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            // This might be a transient error. Consider retry mechanisms or DLQ.
            // For now, re-throwing could cause RabbitMQ to retry (if default behavior or requeue on error is set).
            // Careful with infinite retries for persistent errors.
            // throw e; // or wrap in a specific runtime exception
        }
    }
}