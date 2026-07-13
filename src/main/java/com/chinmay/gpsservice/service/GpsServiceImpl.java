package com.chinmay.gpsservice.service;

import com.chinmay.gpsservice.ExtendedGpsInput;
import com.chinmay.gpsservice.GpsData;
import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.repository.GpsRecordRepository;
import org.slf4j.Logger; // For logging
import org.slf4j.LoggerFactory; // For logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

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
    public GpsRecord saveGpsData(ExtendedGpsInput gpsInput) {
        log.info("Service: Mapping and saving GPS record for publisher: {}", gpsInput.getPublisherId());

        GpsData data = gpsInput.getGpsData();

        // 1. Map DTO to Entity
        GpsRecord record = new GpsRecord();
        record.setPublisherId(gpsInput.getPublisherId());
        record.setLatitude(data.getLatitude().doubleValue());
        record.setLongitude(data.getLongitude().doubleValue());

        if (data.getHeight() != null) {
            record.setHeight(data.getHeight().doubleValue());
        }

        try {
            record.setTimestamp(LocalDateTime.parse(data.getTimeStamp()));
        } catch (DateTimeParseException e) {
            log.error("Invalid timestamp format '{}' for publisher: {}", data.getTimeStamp(), gpsInput.getPublisherId());
            throw new IllegalArgumentException("Invalid timestamp format.", e);
        }

        return gpsRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true) // For read-only operations, this can optimize performance
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
    @Transactional // This operation modifies the database
    public int deleteOldGpsRecords(LocalDateTime cutoffTimestamp) {
        log.info("Service: Deleting records older than {}.", cutoffTimestamp);

        int deletedCount = gpsRecordRepository.deleteRecordsOlderThan(cutoffTimestamp);
        log.info("Service: Successfully deleted {} old GPS records.", deletedCount);
        return deletedCount;
    }
}
