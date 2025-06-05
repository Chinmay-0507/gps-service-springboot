package com.chinmay.GPSService1.service;

import com.chinmay.GPSService1.ExtendedGpsInput;
import com.chinmay.GPSService1.GpsData;
import com.chinmay.GPSService1.entity.GpsRecord;
import com.chinmay.GPSService1.repository.GpsRecordRepository;
import org.junit.jupiter.api.BeforeEach; // For setup before each test
import org.junit.jupiter.api.Test; // Marks a method as a test case
import org.junit.jupiter.api.extension.ExtendWith; // For JUnit 5 extensions
import org.mockito.InjectMocks; // Injects mocks into the tested class
import org.mockito.Mock; // Creates a mock object
import org.mockito.junit.jupiter.MockitoExtension; // Integrates Mockito with JUnit 5

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*; // For assertions like assertEquals, assertThrows
import static org.mockito.ArgumentMatchers.any; // Matches any argument of a certain type
import static org.mockito.Mockito.*; // For Mockito methods like when, verify

@ExtendWith(MockitoExtension.class) // This tells JUnit 5 to use Mockito's extension
class GpsServiceImplTest {

    @Mock // 1. Create a mock of GpsRecordRepository
    private GpsRecordRepository gpsRecordRepository;

    @InjectMocks // 2. Create an instance of GpsServiceImpl and inject the mocks (gpsRecordRepository) into it
    private GpsServiceImpl gpsService;

    // You can define common test data here if needed
    private ExtendedGpsInput validGpsInput;
    private GpsData validGpsData;
    private GpsRecord expectedGpsRecord;

    @BeforeEach // This method runs before each @Test method
    void setUp() {
        // Initialize common test data
        validGpsData = GpsData.builder()
                .latitude(10.0f)
                .longitude(20.0f)
                .timeStamp(LocalDateTime.now().toString()) // Use a valid ISO string
                .height(50.0f)
                .build();

        validGpsInput = ExtendedGpsInput.builder()
                .publisherId("pub123")
                .gpsData(validGpsData)
                .build();

        expectedGpsRecord = new GpsRecord();
        expectedGpsRecord.setId(1L);
        expectedGpsRecord.setPublisherId("pub123");
        expectedGpsRecord.setLatitude(10.0);
        expectedGpsRecord.setLongitude(20.0);
        expectedGpsRecord.setTimestamp(LocalDateTime.parse(validGpsData.getTimeStamp()));
        expectedGpsRecord.setHeight(50.0);
    }

    // Now, let's write individual tests for methods in GpsServiceImpl

    // --- Test for saveGpsData ---
    @Test
    void testSaveGpsData_whenInputIsValid_shouldSaveAndReturnRecord() {
        // Arrange
        // 1. Define what the mock repository should do when its 'save' method is called.
        //    We expect it to be called with any GpsRecord object and then return our 'expectedGpsRecord'.
        when(gpsRecordRepository.save(any(GpsRecord.class))).thenReturn(expectedGpsRecord);

        // Act
        // 2. Call the method we are testing.
        GpsRecord savedRecord = gpsService.saveGpsData(validGpsInput);

        // Assert
        // 3. Verify the outcome.
        assertNotNull(savedRecord, "Saved record should not be null");
        assertEquals(expectedGpsRecord.getId(), savedRecord.getId(), "IDs should match");
        assertEquals("pub123", savedRecord.getPublisherId(), "Publisher IDs should match");
        assertEquals(10.0, savedRecord.getLatitude(), "Latitudes should match");

        // 4. Optionally, verify that the 'save' method on the mock repository was called exactly once.
        verify(gpsRecordRepository, times(1)).save(any(GpsRecord.class));
    }

    @Test
    void testSaveGpsData_whenPublisherIdIsNull_shouldThrowIllegalArgumentException() {
        // Arrange
        ExtendedGpsInput invalidInput = ExtendedGpsInput.builder()
                .publisherId(null) // Invalid state
                .gpsData(validGpsData)
                .build();

        // Act & Assert
        // We expect an IllegalArgumentException to be thrown.
        // The lambda expression contains the call to the method that should throw the exception.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gpsService.saveGpsData(invalidInput);
        });

        assertEquals("Publisher ID cannot be null or empty.", exception.getMessage());

        // Verify that the repository's save method was NEVER called because validation should fail first.
        verify(gpsRecordRepository, never()).save(any(GpsRecord.class));
    }

    @Test
    void testSaveGpsData_whenGpsDataIsNull_shouldThrowIllegalArgumentException() {
        // Arrange
        ExtendedGpsInput invalidInput = ExtendedGpsInput.builder()
                .publisherId("pub123")
                .gpsData(null) // Invalid state
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gpsService.saveGpsData(invalidInput);
        });
        assertEquals("GpsData object cannot be null.", exception.getMessage());
        verify(gpsRecordRepository, never()).save(any(GpsRecord.class));
    }

    @Test
    void testSaveGpsData_whenTimestampIsInvalidFormat_shouldThrowIllegalArgumentException() {
        // Arrange
        GpsData dataWithInvalidTimestamp = GpsData.builder()
                .latitude(10.0f).longitude(20.0f).timeStamp("invalid-date-format").build();
        ExtendedGpsInput inputWithInvalidTimestamp = ExtendedGpsInput.builder()
                .publisherId("pubValid")
                .gpsData(dataWithInvalidTimestamp)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gpsService.saveGpsData(inputWithInvalidTimestamp);
        });
        assertTrue(exception.getMessage().startsWith("Invalid timestamp format."), "Exception message should indicate invalid timestamp format");
        verify(gpsRecordRepository, never()).save(any(GpsRecord.class));
    }


    // --- Test for getAllGpsData ---
    @Test
    void testGetAllGpsData_whenRecordsExist_shouldReturnListOfRecords() {
        // Arrange
        List<GpsRecord> expectedRecords = Arrays.asList(expectedGpsRecord, new GpsRecord()); // Add another record
        when(gpsRecordRepository.findAll()).thenReturn(expectedRecords);

        // Act
        List<GpsRecord> actualRecords = gpsService.getAllGpsData();

        // Assert
        assertNotNull(actualRecords);
        assertEquals(2, actualRecords.size());
        assertEquals(expectedRecords, actualRecords); // Assumes GpsRecord has a proper equals() method (Lombok's @Data provides this)
        verify(gpsRecordRepository, times(1)).findAll();
    }

    @Test
    void testGetAllGpsData_whenNoRecordsExist_shouldReturnEmptyList() {
        // Arrange
        when(gpsRecordRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<GpsRecord> actualRecords = gpsService.getAllGpsData();

        // Assert
        assertNotNull(actualRecords);
        assertTrue(actualRecords.isEmpty());
        verify(gpsRecordRepository, times(1)).findAll();
    }

    // --- Test for getGpsDataByPublisherId ---
    @Test
    void testGetGpsDataByPublisherId_whenIdIsValidAndRecordsExist_shouldReturnList() {
        // Arrange
        String publisherId = "pub123";
        List<GpsRecord> expectedRecords = Collections.singletonList(expectedGpsRecord);
        when(gpsRecordRepository.findByPublisherId(publisherId)).thenReturn(expectedRecords);

        // Act
        List<GpsRecord> actualRecords = gpsService.getGpsDataByPublisherId(publisherId);

        // Assert
        assertNotNull(actualRecords);
        assertEquals(1, actualRecords.size());
        assertEquals(expectedRecords, actualRecords);
        verify(gpsRecordRepository, times(1)).findByPublisherId(publisherId);
    }

    @Test
    void testGetGpsDataByPublisherId_whenIdIsNull_shouldThrowIllegalArgumentException() {
        // Arrange
        String nullPublisherId = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gpsService.getGpsDataByPublisherId(nullPublisherId);
        });
        assertEquals("Publisher ID cannot be null or empty for searching.", exception.getMessage());
        verify(gpsRecordRepository, never()).findByPublisherId(anyString());
    }

    // --- Test for deleteOldGpsRecords ---
    @Test
    void testDeleteOldGpsRecords_shouldCallRepositoryWithCorrectCutoff() {
        // Arrange
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
        int expectedDeletedCount = 5;
        when(gpsRecordRepository.deleteRecordsOlderThan(cutoff)).thenReturn(expectedDeletedCount);

        // Act
        int actualDeletedCount = gpsService.deleteOldGpsRecords(cutoff);

        // Assert
        assertEquals(expectedDeletedCount, actualDeletedCount);
        // Verify that the repository method was called with the exact cutoff time.
        verify(gpsRecordRepository, times(1)).deleteRecordsOlderThan(cutoff);
    }
}