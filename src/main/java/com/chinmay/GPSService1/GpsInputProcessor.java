package com.chinmay.GPSService1;

import com.chinmay.GPSService1.config.RabbitMQConfig; // Import your RabbitMQ constants
import com.chinmay.GPSService1.entity.GpsRecord;     // For GET methods
import com.chinmay.GPSService1.service.GpsService;   // For GET methods
import com.fasterxml.jackson.databind.ObjectMapper;  // For JSON conversion
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // For sending messages
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/gps")
public class GpsInputProcessor { // Renamed from GpsInputProcessor to follow conventions if it was your main class

    private final GpsService gpsService;        // Still needed for GET operations
    private final RabbitTemplate rabbitTemplate;    // Spring's helper for RabbitMQ
    private final ObjectMapper objectMapper;        // For converting objects to JSON strings

    @Autowired
    public GpsInputProcessor(GpsService gpsService, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.gpsService = gpsService;
        this.rabbitTemplate = rabbitTemplate;   // Spring will inject this
        this.objectMapper = objectMapper;       // Spring will inject this (autoconfigured)
    }

    @PostMapping("/putGpsData")
    public ResponseEntity<?> processGpsInput(@RequestBody ExtendedGPSInput extendedGPSInput) {
        log.info("Controller: Received GPS data from publisher '{}' for asynchronous processing via RabbitMQ.",
                extendedGPSInput.getPublisherId());

        try {
            // Optional: Perform very basic, quick validation if necessary before queuing
            if (extendedGPSInput.getPublisherId() == null || extendedGPSInput.getPublisherId().trim().isEmpty()) {
                log.warn("Controller: Publisher ID is null or empty in the request. Rejecting.");
                return ResponseEntity.badRequest().body("Publisher ID is required in the request.");
            }
            if (extendedGPSInput.getGpsData() == null) {
                log.warn("Controller: GPSData object is null in the request. Rejecting.");
                return ResponseEntity.badRequest().body("GPSData object cannot be null in the request.");
            }

            // Convert the incoming DTO to a JSON string to be the message payload
            String messagePayload = objectMapper.writeValueAsString(extendedGPSInput);

            // Send the message to the configured exchange with the routing key
            log.info("Controller: Sending message to exchange '{}' with routing key '{}': Payload='{}'",
                    RabbitMQConfig.GPS_EXCHANGE_NAME, RabbitMQConfig.GPS_DATA_ROUTING_KEY, messagePayload);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.GPS_EXCHANGE_NAME,    // Defined in your RabbitMQConfig
                    RabbitMQConfig.GPS_DATA_ROUTING_KEY, // Defined in your RabbitMQConfig
                    messagePayload);                     // The JSON string

            log.info("Controller: GPS data for publisher '{}' successfully sent to RabbitMQ exchange.",
                    extendedGPSInput.getPublisherId());

            // Return HTTP 202 Accepted: The request has been accepted for processing,
            // but the processing has not been completed. This is a common pattern for async operations.
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("GPS data accepted for processing.");

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Controller: Error serializing ExtendedGPSInput to JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error preparing data for queueing.");
        } catch (Exception e) { // Catch other exceptions, e.g., if RabbitMQ is down or misconfigured
            log.error("Controller: Error sending message to RabbitMQ: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to queue GPS data due to an internal error.");
        }
    }

    // --- GET methods remain largely the same, as they read from the DB after processing ---
    // These methods will still use the GpsService directly to fetch data that has been
    // processed and stored by the listener.

    @GetMapping("/getGpsData")
    public ResponseEntity<List<GpsRecord>> getAllGpsData() {
        log.info("Controller: Request to fetch all GPS data.");
        try {
            List<GpsRecord> records = gpsService.getAllGpsData();
            if (records.isEmpty()) {
                log.info("Controller: No GPS data found.");
                return ResponseEntity.noContent().build();
            }
            log.info("Controller: Returning {} GPS records.", records.size());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Controller: Error fetching all GPS data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getGpsData/{publisherId}")
    public ResponseEntity<?> getGpsDataByPublisherId(@PathVariable String publisherId) {
        log.info("Controller: Request to fetch GPS data for publisherId: {}", publisherId);
        try {
            List<GpsRecord> records = gpsService.getGpsDataByPublisherId(publisherId);
            if (records.isEmpty()) {
                log.info("Controller: No GPS data found for publisherId: {}", publisherId);
                return ResponseEntity.notFound().build();
            }
            log.info("Controller: Returning {} GPS records for publisherId: {}.", records.size(), publisherId);
            return ResponseEntity.ok(records);
        } catch (IllegalArgumentException e) {
            log.warn("Controller: Invalid argument fetching GPS data for publisherId '{}': {}", publisherId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Controller: Error fetching GPS data for publisherId '{}': {}", publisherId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}