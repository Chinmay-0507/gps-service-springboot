package com.chinmay.GPSService1.service;

import com.chinmay.GPSService1.ExtendedGPSInput; // Your input DTO
import com.chinmay.GPSService1.entity.GpsRecord;   // Your entity

import java.time.LocalDateTime;
import java.util.List;

public interface GpsService {

    /**
     * Saves the given GPS input data to the database.
     * @param gpsInput The GPS data to save, encapsulated in ExtendedGPSInput DTO.
     * @return The saved GpsRecord entity, including its generated ID.
     * @throws IllegalArgumentException if the input is invalid.
     */
    GpsRecord saveGpsData(ExtendedGPSInput gpsInput);

    /**
     * Retrieves all GPS records from the database.
     * @return A list of all GpsRecord entities.
     */
    List<GpsRecord> getAllGpsData();

    /**
     * Retrieves GPS records for a specific publisher ID.
     * @param publisherId The ID of the publisher.
     * @return A list of GpsRecord entities for the given publisher.
     * @throws IllegalArgumentException if the publisherId is null or empty.
     */
    List<GpsRecord> getGpsDataByPublisherId(String publisherId);

    // You might add other methods later, such as:
    // GpsRecord getGpsDataById(Long id);
    // void deleteGpsData(Long id);
    // GpsRecord updateGpsData(Long id, ExtendedGPSInput gpsInput);

    /**
     * Deletes GPS records with a timestamp older than the provided cutoff.
     * @param cutoffTimestamp Records with a timestamp before this will be deleted.
     * @return The number of records deleted.
     */
    int deleteOldGpsRecords(LocalDateTime cutoffTimestamp);
}
