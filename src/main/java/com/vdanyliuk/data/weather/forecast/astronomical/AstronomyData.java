package com.vdanyliuk.data.weather.forecast.astronomical;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AstronomyData implements Serializable{

    private static final long serialVersionUID = 1L;

    private LocalDate date;

    private double astronomicalDayLong;

    private double dayLightLong;

    private LocalTime sunRise;

    private LocalTime sunSet;
}
