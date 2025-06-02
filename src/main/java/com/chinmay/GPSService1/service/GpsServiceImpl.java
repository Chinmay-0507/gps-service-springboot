package com.chinmay.GPSService1.service;

import com.chinmay.GPSService1.ExtendedGPSInput;
import com.chinmay.GPSService1.GPSData; // Your nested DTO
import com.chinmay.GPSService1.entity.GpsRecord;
import com.chinmay.GPSService1.repository.GpsRecordRepository; // << IMPORT YOUR REPOSITORY
import org.slf4j.Logger; // For logging
import org.slf4j.LoggerFactory; // For logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For database transactions

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects; // For null checks

@Service // Marks this class as a Spring service component, making it eligible for dependency injection
public class GpsServiceImpl implements GpsService {

    private static final Logger log = LoggerFactory.getLogger(GpsServiceImpl.class); // Logger instance

    private final GpsRecordRepository gpsRecordRepository; // Dependency

    @Autowired // Constructor injection is the recommended way to inject dependencies
    public GpsServiceImpl(GpsRecordRepository gpsRecordRepository) {
        this.gpsRecordRepository = gpsRecordRepository;
    }

    @Override
    @Transactional // Ensures this method runs within a database transaction. If an error occurs, changes are rolled back.
    public GpsRecord saveGpsData(ExtendedGPSInput gpsInput) {
        // 1. Validate Input DTO
        if (gpsInput == null) {
            log.error("GPS input data is null.");
            throw new IllegalArgumentException("GPS input data cannot be null.");
        }
        if (gpsInput.getPublisherId() == null || gpsInput.getPublisherId().trim().isEmpty()) {
            log.error("Publisher ID is null or empty.");
            throw new IllegalArgumentException("Publisher ID cannot be null or empty.");
        }
        if (gpsInput.getGpsData() == null) {
            log.error("Nested GPSData object is null for publisher: {}", gpsInput.getPublisherId());
            throw new IllegalArgumentException("GPSData object cannot be null.");
        }

        GPSData data = gpsInput.getGpsData();
        if (data.getTimeStamp() == null || data.getTimeStamp().trim().isEmpty()) {
            log.error("Timestamp is null or empty for publisher: {}", gpsInput.getPublisherId());
            throw new IllegalArgumentException("Timestamp cannot be null or empty.");
        }
        if (data.getLatitude() == null) {
            log.error("Latitude is null for publisher: {}", gpsInput.getPublisherId());
            throw new IllegalArgumentException("Latitude cannot be null.");
        }
        if (data.getLongitude() == null) {
            log.error("Longitude is null for publisher: {}", gpsInput.getPublisherId());
            throw new IllegalArgumentException("Longitude cannot be null.");
        }


        // 2. Map DTO to Entity
        GpsRecord record = new GpsRecord();
        record.setPublisherId(gpsInput.getPublisherId());

        // Convert Float from DTO to Double for Entity
        record.setLatitude(data.getLatitude().doubleValue());
        record.setLongitude(data.getLongitude().doubleValue());
        if (data.getHeight() != null) { // Height is optional
            record.setHeight(data.getHeight().doubleValue());
        }

        try {
            // Assumes timestamp string is in ISO_LOCAL_DATE_TIME format e.g., "2023-10-27T10:15:30"
            record.setTimestamp(LocalDateTime.parse(data.getTimeStamp()));
        } catch (DateTimeParseException e) {
            log.error("Invalid timestamp format '{}' for publisher: {}. Expected ISO format (e.g., yyyy-MM-ddTHH:mm:ss).",
                    data.getTimeStamp(), gpsInput.getPublisherId(), e);
            throw new IllegalArgumentException("Invalid timestamp format. Expected ISO_LOCAL_DATE_TIME (e.g., yyyy-MM-ddTHH:mm:ss).", e);
        }

        // 3. Save Entity using Repository
            log.info("Saving GPS record for publisher: {}", record.getPublisherId());
        return gpsRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true) // For read-only operations, this can optimize performance slightly
    public List<GpsRecord> getAllGpsData() {
        log.info("Fetching all GPS data records.");
        return gpsRecordRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsRecord> getGpsDataByPublisherId(String publisherId) {
        if (publisherId == null || publisherId.trim().isEmpty()) {
            log.warn("Attempted to fetch GPS data with null or empty publisherId.");
            throw new IllegalArgumentException("Publisher ID cannot be null or empty for searching.");
        }
        log.info("Fetching GPS data for publisherId: {}", publisherId);
        return gpsRecordRepository.findByPublisherId(publisherId);
    }

    @Override
    @Transactional // Important: This operation modifies the database
    public int deleteOldGpsRecords(LocalDateTime cutoffTimestamp) {
        log.info("Service: Deleting records older than {}.", cutoffTimestamp);
        // Call the repository method that executes the custom delete query
        int deletedCount = gpsRecordRepository.deleteRecordsOlderThan(cutoffTimestamp);
        log.info("Service: Successfully deleted {} old GPS records.", deletedCount);
        return deletedCount;
    }
}
