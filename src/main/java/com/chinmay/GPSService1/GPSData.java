package com.chinmay.GPSService1;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
public class GPSData {

    private Float latitude;

    private Float longitude;

    private Float height;

    private String timeStamp;
}
