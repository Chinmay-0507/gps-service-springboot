package com.chinmay.gpsservice.service;

import com.chinmay.gpsservice.ExtendedGpsInput;
import com.chinmay.gpsservice.GpsData;
import com.chinmay.gpsservice.entity.GpsRecord;
import com.chinmay.gpsservice.repository.GpsRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpsServiceImplTest {

    @Mock
    private GpsRecordRepository gpsRecordRepository;

    @InjectMocks
    private GpsServiceImpl gpsService;

    private ExtendedGpsInput validGpsInput;
    private GpsData validGpsData;

    @BeforeEach
    void setUp() {
        validGpsData = GpsData.builder()
                .latitude(40.7128f)
                .longitude(-74.0060f)
                .height(10.5f)
                .timeStamp("2026-07-13T10:00:00") // Valid ISO Format
                .build();

        validGpsInput = ExtendedGpsInput.builder()
                .publisherId("TRUCK-01")
                .gpsData(validGpsData)
                .build();
    }

    @Test
    void testSaveGpsData_ValidInput_ShouldMapAndSave() {
        GpsRecord mockSavedRecord = new GpsRecord();
        mockSavedRecord.setId(1L);
        when(gpsRecordRepository.save(any(GpsRecord.class))).thenReturn(mockSavedRecord);

        GpsRecord result = gpsService.saveGpsData(validGpsInput);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Advanced Verification: Did the Service map the DTO to the Entity correctly?
        // We capture the exact object the Service tried to save to the database.
        ArgumentCaptor<GpsRecord> recordCaptor = ArgumentCaptor.forClass(GpsRecord.class);
        verify(gpsRecordRepository, times(1)).save(recordCaptor.capture());

        GpsRecord capturedRecord = recordCaptor.getValue();
        assertEquals("TRUCK-01", capturedRecord.getPublisherId());
        assertEquals(40.7128, capturedRecord.getLatitude());
        assertEquals(LocalDateTime.of(2026, 7, 13, 10, 0, 0), capturedRecord.getTimestamp());
    }

    @Test
    void testSaveGpsData_InvalidTimestamp_ShouldThrowException() {
        validGpsData.setTimeStamp("this-is-not-a-date");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gpsService.saveGpsData(validGpsInput);
        });

        assertTrue(exception.getMessage().contains("Invalid timestamp format"));

        verify(gpsRecordRepository, never()).save(any(GpsRecord.class));
    }

    @Test
    void testGetGpsDataByPublisherId_ShouldReturnList() {
        // Arrange
        GpsRecord fakeRecord = new GpsRecord();
        fakeRecord.setPublisherId("TRUCK-01");
        when(gpsRecordRepository.findByPublisherId("TRUCK-01")).thenReturn(Collections.singletonList(fakeRecord));

        List<GpsRecord> result = gpsService.getGpsDataByPublisherId("TRUCK-01");

        assertEquals(1, result.size());
        assertEquals("TRUCK-01", result.get(0).getPublisherId());
        verify(gpsRecordRepository, times(1)).findByPublisherId("TRUCK-01");
    }

    @Test
    void testDeleteOldGpsRecords_ShouldReturnDeletedCount() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        when(gpsRecordRepository.deleteRecordsOlderThan(cutoff)).thenReturn(50); // Pretend we deleted 50 records

        int deletedCount = gpsService.deleteOldGpsRecords(cutoff);

        assertEquals(50, deletedCount);
        verify(gpsRecordRepository, times(1)).deleteRecordsOlderThan(cutoff);
    }
}