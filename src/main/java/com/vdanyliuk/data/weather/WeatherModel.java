package com.vdanyliuk.data.weather;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class WeatherModel implements Serializable{

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private LocalDate date;

    private double avgTemperature;
    private double minTemperature;
    private double maxTemperature;

    private double dewPoint;

    private double avgHumidity;
    private double maxHumidity;
    private double minHumidity;

    private double precipitation;

    private double pressure;

    private double wind;

    private double visibility;

    private double astronomicalDayLong;
    private double dayLightLong;

    private double sunRiseBeforeWork;
    private double sunSetBeforeWork;

    private double clouds;

    public double getAvgTemperature() {
        return avgTemperature + 273.15;
    }

    public double getMinTemperature() {
        return minTemperature + 273.15;
    }

    public double getMaxTemperature() {
        return maxTemperature + 273.15;
    }
}
