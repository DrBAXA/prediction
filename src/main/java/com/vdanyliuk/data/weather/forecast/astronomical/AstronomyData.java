package com.vdanyliuk.data.weather.forecast.astronomical;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AstronomyData {

    private LocalDate date;

    private double astronomicalDayLong;

    private double dayLightLong;

    private LocalTime sunRise;

    private LocalTime sunSet;
}
