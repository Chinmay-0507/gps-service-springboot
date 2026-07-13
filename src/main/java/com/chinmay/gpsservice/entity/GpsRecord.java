package com.chinmay.gpsservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gps_records")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class GpsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String publisherId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double height;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime timestamp;
}

