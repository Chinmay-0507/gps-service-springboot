package com.chinmay.gpsservice.service;

import java.time.LocalDateTime;

public interface GpsAnalyticsService {
    double calculateTotalDistance(String publisherId, LocalDateTime from, LocalDateTime to);
}