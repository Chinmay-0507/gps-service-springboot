package com.chinmay.gpsservice.controller;

import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.repository.GpsRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class DataSeederController {

    private final GpsRecordRepository repository;

    @PostMapping("/seed")
    public String seedDatabase() {
        // injecting 50,000 records.
        int totalRecords = 50000;
        int batchSize = 1000;

        // Start the timestamps 30 days ago
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);

        log.info("Starting database seed... This might take a few seconds.");

        for (int i = 0; i < totalRecords / batchSize; i++) {
            List<GpsRecord> batch = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                GpsRecord record = new GpsRecord();
                // We assign all 50,000 points to TRUCK-99 so we can query it easily
                record.setPublisherId("TRUCK-99");

                // Generate random coordinates
                record.setLatitude(40.0 + (Math.random() * 5));
                record.setLongitude(-70.0 - (Math.random() * 5));

                // Space the timestamps out by 1 minute each
                record.setTimestamp(startTime.plusMinutes((i * batchSize) + j));

                batch.add(record);
            }
            // Save 1,000 at a time
            repository.saveAll(batch);
            log.info("Saved batch {} of {}", (i + 1), (totalRecords / batchSize));
        }

        log.info("Finished seeding 50,000 records!");
        return "Successfully injected 50,000 records into the database!";
    }
}