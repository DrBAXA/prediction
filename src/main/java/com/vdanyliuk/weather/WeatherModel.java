package com.vdanyliuk.weather;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class WeatherModel {

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

    @Override
    public String toString() {
        return        FORMATTER.format(date) +
                "," + avgTemperature +
                "," + minTemperature +
                "," + maxTemperature +
                "," + dewPoint +
                "," + avgHumidity +
                "," + maxHumidity +
                "," + minHumidity +
                "," + precipitation +
                "," + pressure +
                "," + wind +
                "," + visibility +
                "," + astronomicalDayLong +
                "," + dayLightLong;
    }
}
