package com.chinmay.gpsservice.util;

public class HaversineUtil {

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the great-circle distance between two GPS points on Earth.
     * @return Distance in kilometers.
     */
    public static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        double startLatRad = Math.toRadians(startLat);
        double endLatRad = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(startLatRad) * Math.cos(endLatRad) *
                        Math.pow(Math.sin(dLong / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}