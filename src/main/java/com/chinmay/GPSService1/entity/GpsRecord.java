package com.chinmay.GPSService1.entity;

import jakarta.persistence.*; // 2. JPA Imports (jakarta.* for Spring Boot 3+, javax.* for Spring Boot 2.x)
// 3. Lombok Import
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity // 5. @Entity Annotation
@Table(name = "gps_records") // 6. @Table Annotation
@Data // 7. Lombok @Data Annotation
@NoArgsConstructor // 8. Lombok @NoArgsConstructor
@AllArgsConstructor // 9. Lombok @AllArgsConstructor

public class GpsRecord {
    @Id // 11. @Id Annotation
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 12. @GeneratedValue Annotation
    private Long id; // 13. Primary Key Field

    @Column(nullable = false, length = 100) // 14. @Column Annotation (with constraints)
    private String publisherId; // 15. Field for publisherId

    @Column(nullable = false)
    private Double latitude; // 16. Field for latitude

    @Column(nullable = false)
    private Double longitude; // 17. Field for longitude

    private Double height; // 18. Field for height (nullable by default)

    @Column(name = "event_timestamp", nullable = false) // 19. @Column with custom name
    private LocalDateTime timestamp; // 20. Field for timestamp
}

