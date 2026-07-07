package com.chinmay.gpsservice.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HaversineUtilTest {

    @Test
    void testCalculateDistance_NewYorkToLondon() {
        // Coordinates for New York (JFK)
        double lat1 = 40.6413;
        double lon1 = -73.7781;

        // Coordinates for London (Heathrow)
        double lat2 = 51.4700;
        double lon2 = -0.4543;

        double distance = HaversineUtil.calculateDistance(lat1, lon1, lat2, lon2);

        // The actual distance is roughly 5550 km. We use a delta of 50km to account for Earth's slight imperfect sphere shape.
        assertEquals(5550.0, distance, 50.0, "Distance between NY and London should be approx 5550km");
    }

    @Test
    void testCalculateDistance_SamePoint_ShouldBeZero() {
        double lat = 40.6413;
        double lon = -73.7781;

        double distance = HaversineUtil.calculateDistance(lat, lon, lat, lon);

        assertEquals(0.0, distance, "Distance to the exact same point should be 0");
    }
}\\\