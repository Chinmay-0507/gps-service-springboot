package com.chinmay.gpsservice.service.AnalyticsService;

import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.repository.GpsRecordRepository;
import com.chinmay.gpsservice.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor // Lombok generates a constructor for 'final' fields automatically!
public class GpsAnalyticsServiceImpl implements GpsAnalyticsService {

    private final GpsRecordRepository repository;

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalDistance(String publisherId, LocalDateTime from, LocalDateTime to) {
        log.info("Calculating route distance for publisher: {} from {} to {}", publisherId, from, to);

        // 1. Fetch the chronologically sorted route
        List<GpsRecord> route = repository.findByPublisherIdAndTimestampBetweenOrderByTimestampAsc(
                publisherId, from, to);

        // 2. Base case: If 0 or 1 points exist, no distance was traveled
        if (route == null || route.size() < 2) {
            log.info("Not enough data points to calculate distance for {}", publisherId);
            return 0.0;
        }

        double totalDistanceKm = 0.0;

        // 3. Connect the dots: Compare point [i] to point [i-1]
        for (int i = 1; i < route.size(); i++) {
            GpsRecord prev = route.get(i - 1);
            GpsRecord curr = route.get(i);

            double segmentDistance = HaversineUtil.calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );

            totalDistanceKm += segmentDistance;
        }

        log.info("Total distance for {}: {} km", publisherId, totalDistanceKm);
        return totalDistanceKm;
    }
}