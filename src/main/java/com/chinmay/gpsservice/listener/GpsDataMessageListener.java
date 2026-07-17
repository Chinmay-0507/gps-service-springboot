package com.chinmay.gpsservice.listener; // New package for listeners

import com.chinmay.gpsservice.dto.ExtendedGpsInput;    // Your dto
import com.chinmay.gpsservice.config.RabbitMQConfig; // Your RabbitMQ constants
import com.chinmay.gpsservice.service.GpsService;   // Your existing service
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

    @RabbitListener(queues = RabbitMQConfig.GPS_DATA_QUEUE_NAME)
    public void handleIncomingGpsData(String messagePayload) { // Receives the message body as a String
        log.info("Listener: Received message from queue '{}'. Payload='{}'",
                RabbitMQConfig.GPS_DATA_QUEUE_NAME, messagePayload);

        try {
            ExtendedGpsInput gpsInput = objectMapper.readValue(messagePayload, ExtendedGpsInput.class);

            if (gpsInput.getPublisherId().equals("CRASH-TEST")) {
                throw new RuntimeException("Simulated Database Timeout Error!");
            }

            log.info("Listener: Successfully deserialized message for publisher '{}'. Attempting to save data.",
                    gpsInput.getPublisherId());

            gpsService.saveGpsData(gpsInput); // This will go through your service's validation and DB save

            log.info("Listener: Successfully processed and saved GPS data from queue for publisher '{}'.",
                    gpsInput.getPublisherId());

        } catch (IllegalArgumentException e) {
            log.error("Listener: Validation error while processing GPS data from queue. Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Validation failed: " + e.getMessage()); // Example for DLQ
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Listener: Error deserializing message from queue (malformed JSON). Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Malformed JSON message", e);
        } catch (Exception e) {
            log.error("Listener: Unexpected error processing GPS data message from queue. Message Payload='{}'. Error: {}",
                    messagePayload, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unexpected system error", e);
        }
    }
}