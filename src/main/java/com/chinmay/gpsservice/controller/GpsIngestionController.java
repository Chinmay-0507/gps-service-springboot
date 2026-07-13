package com.chinmay.gpsservice.controller;

import com.chinmay.gpsservice.dto.ExtendedGpsInput;
import com.chinmay.gpsservice.config.RabbitMQConfig;
import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.service.GpsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/gps")
@RequiredArgsConstructor // Automatically creates constructor for 'final' variables
public class GpsIngestionController {

    private final GpsService gpsService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/putGpsData")
    public ResponseEntity<?> processGpsInput(@Valid @RequestBody ExtendedGpsInput extendedGPSInput) {
        boolean IS_ASYNC_MODE = true;
        log.info("Ingestion: Received validated GPS data from publisher '{}'", extendedGPSInput.getPublisherId());

        try {
            if (IS_ASYNC_MODE) {
                String messagePayload = objectMapper.writeValueAsString(extendedGPSInput);
                rabbitTemplate.convertAndSend(RabbitMQConfig.GPS_EXCHANGE_NAME, RabbitMQConfig.GPS_DATA_ROUTING_KEY, messagePayload);
                log.info("Ingestion (ASYNC): Data for '{}' safely queued to RabbitMQ.", extendedGPSInput.getPublisherId());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("GPS data accepted for asynchronous processing.");
            } else {
                GpsRecord savedRecord = gpsService.saveGpsData(extendedGPSInput);
                log.info("Ingestion (SYNC): Data saved. ID: {}", savedRecord.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body("GPS data saved synchronously. Record ID: " + savedRecord.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("Ingestion: Error converting payload to JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error preparing data for queueing.");
        } catch (Exception e) {
            log.error("Ingestion: Unexpected system error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process GPS data due to an internal error.");
        }
    }
}
