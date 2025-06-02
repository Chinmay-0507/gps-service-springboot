package com.chinmay.GPSService1.repository;

import com.chinmay.GPSService1.entity.GpsRecord; // << IMPORT YOUR ENTITY HERE
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Optional, but good practice

import java.time.LocalDateTime;
import java.util.List;

@Repository // Marks this as a Spring Data repository bean. Spring Boot can often find it without this, but it's good for clarity

public interface GpsRecordRepository extends JpaRepository<GpsRecord, Long> {
    // 1. JpaRepository<EntityType, IdType>:
    //    - GpsRecord: This tells Spring Data JPA that this repository works with GpsRecord entities.
    //    - Long: This is the data type of the primary key (@Id field) in your GpsRecord entity.

    // 2. Spring Data JPA will automatically provide implementations for common CRUD methods like:
    //    - save(GpsRecord entity)
    //    - findById(Long id)
    //    - findAll()
    //    - deleteById(Long id)
    //    - count()
    //    - existsById(Long id)
    //    - ...and many more. You don't need to write any code for these!

    // 3. You can define custom query methods by following naming conventions.
    //    Spring Data JPA will parse the method name and generate the query.
    //    For example, to find all GPS records for a specific publisherId:
    List<GpsRecord> findByPublisherId(String publisherId);

    // Another example: find by latitude
    // List<GpsRecord> findByLatitude(Double latitude);
    @Modifying
    @Query("DELETE FROM GpsRecord gr WHERE gr.timestamp < :cutoffTimestamp")
    int deleteRecordsOlderThan(@Param("cutoffTimestamp") LocalDateTime cutoffTimestamp);
}
