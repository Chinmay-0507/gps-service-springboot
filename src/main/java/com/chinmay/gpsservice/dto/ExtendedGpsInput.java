package com.chinmay.gpsservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
public class ExtendedGpsInput {

    @NotBlank(message = "Publisher ID cannot be blank")
    private String publisherId;

    @NotNull(message = "GPS data payload is required")
    @Valid
    private GpsData gpsData;
}