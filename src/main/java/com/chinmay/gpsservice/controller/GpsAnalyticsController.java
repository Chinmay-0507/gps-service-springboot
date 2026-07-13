package com.chinmay.gpsservice.controller;

import com.chinmay.gpsservice.service.GpsAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/api/gps/analytics")
@RequiredArgsConstructor
public class GpsAnalyticsController {

    private final GpsAnalyticsService gpsAnalyticsService;

    @GetMapping("/{publisherId}/distance")
    public ResponseEntity<?> getTotalDistance(
            @PathVariable String publisherId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("Analytics: Requesting distance for {} between {} and {}", publisherId, from, to);

        try {
            if (from.isAfter(to)) {
                return ResponseEntity.badRequest().body("'from' date cannot be after 'to' date.");
            }
            double distanceKm = gpsAnalyticsService.calculateTotalDistance(publisherId, from, to);
            return ResponseEntity.ok(String.format("{\"publisherId\": \"%s\", \"totalDistanceKm\": %.2f}", publisherId, distanceKm));
        } catch (Exception e) {
            log.error("Analytics: Error calculating distance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to calculate distance");
        }
    }
}
