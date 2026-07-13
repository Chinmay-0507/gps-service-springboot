package com.chinmay.gpsservice.controller;

import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.service.GpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/gps/query")
@RequiredArgsConstructor
public class GpsQueryController {

    private final GpsService gpsService;

    @GetMapping("/all")
    public ResponseEntity<List<GpsRecord>> getAllGpsData() {
        log.info("Query: Request to fetch all GPS data.");
        try {
            List<GpsRecord> records = gpsService.getAllGpsData();
            if (records.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Query: Error fetching all GPS data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{publisherId}")
    public ResponseEntity<?> getGpsDataByPublisherId(@PathVariable String publisherId) {
        log.info("Query: Request to fetch GPS data for publisherId: {}", publisherId);
        try {
            List<GpsRecord> records = gpsService.getGpsDataByPublisherId(publisherId);
            if (records.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Query: Error fetching GPS data for publisher '{}': {}", publisherId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
