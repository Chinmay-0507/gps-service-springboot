package com.chinmay.gpsservice.service;

import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.repository.GpsRecordRepository;
import com.chinmay.gpsservice.service.AnalyticsService.GpsAnalyticsServiceImpl;
import com.chinmay.gpsservice.util.HaversineUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GpsAnalyticsServiceImplTest {

    @Mock
    private GpsRecordRepository repository;

    @InjectMocks
    private GpsAnalyticsServiceImpl analyticsService;

    private GpsRecord point1;
    private GpsRecord point2;

    @BeforeEach // this annotation is used to say that use these sample points before each unit test function
    void setUp() {
        // Point A: New York
        point1 = new GpsRecord();
        point1.setLatitude(40.6413);
        point1.setLongitude(-73.7781);
        point1.setTimestamp(LocalDateTime.now().minusHours(2));

        // Point B: London
        point2 = new GpsRecord();
        point2.setLatitude(51.4700);
        point2.setLongitude(-0.4543);
        point2.setTimestamp(LocalDateTime.now().minusHours(1));
    }

    @Test
    void testCalculateTotalDistance_WithValidRoute() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusHours(3);
        LocalDateTime to = LocalDateTime.now();
        String publisherId = "TRUCK-01";

        // Mock the database to return our two points
        when(repository.findByPublisherIdAndTimestampBetweenOrderByTimestampAsc(publisherId, from, to))
                .thenReturn(Arrays.asList(point1, point2));

        double distance = analyticsService.calculateTotalDistance(publisherId, from, to);

        // Assert: Distance between NY and London should be roughly 5550km with delta i.e deviation of 50kms
        assertEquals(5550.0, distance, 50.0, "Distance calculation is incorrect");
    }

    @Test
    void testCalculateTotalDistance_NotEnoughPoints_ReturnsZero() {
        LocalDateTime from = LocalDateTime.now().minusHours(3);
        LocalDateTime to = LocalDateTime.now();
        String publisherId = "TRUCK-01";

        // Mock the database to return ONLY ONE point
        when(repository.findByPublisherIdAndTimestampBetweenOrderByTimestampAsc(publisherId, from, to))
                .thenReturn(Collections.singletonList(point1));

        double distance = analyticsService.calculateTotalDistance(publisherId, from, to);

        // Assert: If the truck hasn't moved to a second point, distance is 0
        assertEquals(0.0, distance, "Distance should be 0 if there is only 1 point");
    }

    @Test //
    void testReversibilityBetweenTwoPoints() {

        double latA = 40.7128;
        double lonA = -74.0060;

        double latB = 51.5074;
        double lonB = -0.1278;

        double distanceAtoB = HaversineUtil.calculateDistance(latA, lonA, latB, lonB);
        double distanceBtoA = HaversineUtil.calculateDistance(latB, lonB, latA, lonA); // Swapped!

        assertEquals(distanceAtoB, distanceBtoA, 0.001, "Distance from A->B should perfectly match B->A");
    }
}