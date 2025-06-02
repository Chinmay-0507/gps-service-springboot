package com.chinmay.GPSService1.scheduler;

import com.chinmay.GPSService1.entity.GpsRecord;
import com.chinmay.GPSService1.service.GpsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // For injecting properties
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component // So Spring manages this bean and detects @Scheduled methods
@Slf4j
public class DataMaintenanceScheduler {

    private final GpsService gpsService;

    // Inject the retention period from application.properties
    // If the property is not found, it defaults to 90 days.
    @Value("${gps.data.retention.days:90}")
    private int dataRetentionDays;

    @Autowired
    public DataMaintenanceScheduler(GpsService gpsService) {
        this.gpsService = gpsService;
    }

    /**
     * Scheduled task to periodically delete old GPS records.
     * Runs every day at 2:00 AM server time.
     * Cron expression: second minute hour day-of-month month day-of-week
     * Example: "0 0 2 * * ?" means at 2:00:00 AM every day.
     */
    // Inside DataMaintenanceScheduler.java's cleanupOldGpsData method

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds FOR TESTING
    public void cleanupOldGpsData() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime cutoffTime = currentTime.minusDays(dataRetentionDays); // Use configured retention

        log.info("Scheduled Task: Initiating cleanup of GPS data older than {} ({} days retention). Current time: {}",
                cutoffTime, dataRetentionDays, currentTime);
        try {
            int deletedCount = gpsService.deleteOldGpsRecords(cutoffTime);
            // ... (existing logging for deleted count) ...

            // <<<< ADD VISUAL CHECK LOGGING HERE >>>>
            log.info("VISUAL CHECK (Post-Cleanup): Fetching all remaining GPS data at {}", LocalDateTime.now());
            List<GpsRecord> remainingRecords = gpsService.getAllGpsData();
            if (remainingRecords.isEmpty()) {
                log.info("VISUAL CHECK (Post-Cleanup): No GPS records found in the database.");
            } else {
                log.info("VISUAL CHECK (Post-Cleanup): --- Remaining GPS Records (Count: {}) ---", remainingRecords.size());
                remainingRecords.forEach(record ->
                        log.info("VISUAL CHECK (Post-Cleanup): ID: {}, Publisher: {}, Timestamp: {}",
                                record.getId(), record.getPublisherId(), record.getTimestamp())
                );
                log.info("VISUAL CHECK (Post-Cleanup): --- End of Remaining GPS Records ---");
            }
            // <<<< END OF VISUAL CHECK LOGGING >>>>

        } catch (Exception e) {
            log.error("Scheduled Task: An error occurred during old GPS data cleanup: {}", e.getMessage(), e);
        }
    }
}