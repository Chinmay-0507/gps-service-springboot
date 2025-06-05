package com.chinmay.GPSService1;

import com.chinmay.GPSService1.config.RabbitMQConfig;
import com.chinmay.GPSService1.entity.GpsRecord;     // Assuming Entity package
import com.chinmay.GPSService1.service.GpsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //
@Slf4j
@RequestMapping("/api/gps") //
public class GpsInputProcessor {

    private final GpsService gpsService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GpsInputProcessor(GpsService gpsService, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.gpsService = gpsService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/putGpsData") // This single endpoint will be used for both tests
    public ResponseEntity<?> processGpsInput(@RequestBody ExtendedGpsInput extendedGPSInput) {
        // Decide which mode you are testing: ASYNC or SYNC
        boolean IS_ASYNC_MODE = true; //  CHANGE THIS TO 'false' FOR SYNCHRONOUS TEST AND TRUE FOR ASYNCHRONOUS

        if (IS_ASYNC_MODE) {
            log.info("Controller (ASYNC MODE): Received GPS data from publisher '{}' for asynchronous processing.",
                    extendedGPSInput.getPublisherId());
        } else {
            log.info("Controller (SYNC MODE): Received GPS data from publisher '{}' for synchronous processing.",
                    extendedGPSInput.getPublisherId());
        }

        try {
            // Perform very basic, quick validation if necessary before queueing/saving
            if (extendedGPSInput.getPublisherId() == null || extendedGPSInput.getPublisherId().trim().isEmpty()) {
                log.warn("Controller: Publisher ID is null or empty in the request. Rejecting.");
                return ResponseEntity.badRequest().body("Publisher ID is required in the request.");
            }
            if (extendedGPSInput.getGpsData() == null) {
                log.warn("Controller: GPSData object is null in the request. Rejecting.");
                return ResponseEntity.badRequest().body("GPSData object cannot be null in the request.");
            }

            // --- TOGGLE BETWEEN ASYNC AND SYNC LOGIC ---
            if (IS_ASYNC_MODE) {
                // ASYNCHRONOUS PATH (using RabbitMQ)
                String messagePayload = objectMapper.writeValueAsString(extendedGPSInput);
                log.info("Controller (ASYNC MODE): Sending message to exchange '{}' with routing key '{}': Payload-omitted",
                        RabbitMQConfig.GPS_EXCHANGE_NAME, RabbitMQConfig.GPS_DATA_ROUTING_KEY);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.GPS_EXCHANGE_NAME,
                        RabbitMQConfig.GPS_DATA_ROUTING_KEY,
                        messagePayload
                );
                log.info("Controller (ASYNC MODE): GPS data for publisher '{}' successfully sent to RabbitMQ exchange.",
                        extendedGPSInput.getPublisherId());
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body("GPS data accepted for asynchronous processing.");
            } else {
                // SYNCHRONOUS PATH (direct service call)
                GpsRecord savedRecord = gpsService.saveGpsData(extendedGPSInput); // This does full validation + DB save
                log.info("Controller (SYNC MODE): GPS data for publisher '{}' successfully saved. Record ID: {}",
                        savedRecord.getPublisherId(), savedRecord.getId());
                return ResponseEntity.status(HttpStatus.CREATED) // Or HttpStatus.OK
                        .body("GPS data saved synchronously. Record ID: " + savedRecord.getId());
            }

        } catch (JsonProcessingException e) { // Specifically for ASYNC objectMapper.writeValueAsString
            log.error("Controller (ASYNC MODE): Error serializing ExtendedGPSInput to JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error preparing data for queueing.");
        } catch (IllegalArgumentException e) { // Specifically for SYNC GpsService validation
            if (!IS_ASYNC_MODE) { // Only relevant for sync mode here
                log.warn("Controller (SYNC MODE): Validation error during synchronous save for publisher '{}': {}",
                        extendedGPSInput.getPublisherId(), e.getMessage());
                return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
            }
            // If it's an IllegalArgumentException in ASYNC mode from basic validation above, it's already handled.
            // This specific catch is more for GpsService's detailed validation in SYNC mode.
            // For robustness, you might have a more general error handler below this.
            log.error("Controller: Unexpected IllegalArgumentException: {}", e.getMessage(), e); // Should ideally not hit here in ASYNC if basic validation is good
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        } catch (Exception e) { // General catch-all
            if (IS_ASYNC_MODE) {
                log.error("Controller (ASYNC MODE): Error sending message to RabbitMQ or other issue: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to queue GPS data due to an internal error.");
            } else {
                log.error("Controller (SYNC MODE): Error saving GPS data synchronously for publisher '{}': {}",
                        extendedGPSInput.getPublisherId(), e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save GPS data synchronously due to an internal error.");
            }
        }
    }

    // --- GET methods remain largely the same ---
    @GetMapping("/getAllGpsData")
    public ResponseEntity<List<GpsRecord>> getAllGpsData() {
        // ... (your existing GET logic) ...
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
        // ... (your existing GET logic) ...
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