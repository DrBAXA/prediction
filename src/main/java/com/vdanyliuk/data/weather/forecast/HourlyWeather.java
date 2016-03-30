package com.vdanyliuk.data.weather.forecast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyWeather {
    @JsonProperty("time")
    private long timestamp;

    @JsonProperty("temperature")
    private double temperature;

    @JsonProperty("dewPoint")
    private double dewPoint;

    @JsonProperty("humidity")
    private double humidity;

    @JsonProperty("pressure")
    private double pressure;

    @JsonProperty("windSpeed")
    private double wind;

    @JsonProperty("visibility")
    private double visibility;

    @JsonProperty("cloudCover")
    private double clouds;

    @JsonProperty("precipIntensity")
    private double perception;

    @AllArgsConstructor
    @Getter
    public static class Averager {

        private final long timestamp;
        private final double temperature;
        private final double dewPoint;
        private final double humidity;
        private final double pressure;
        private final double wind;
        private final double visibility;
        private final double clouds;
        private double perception;

        private final long maxTimestamp;
        private final double maxTemperature;
        private final double maxDewPoint;
        private final double maxHumidity;
        private final double maxPressure;
        private final double maxWind;
        private final double maxVisibility;
        private final double maxClouds;
        private double maxPerception;

        private final long minTimestamp;
        private final double minTemperature;
        private final double minDewPoint;
        private final double minHumidity;
        private final double minPressure;
        private final double minWind;
        private final double minVisibility;
        private final double minClouds;
        private double minPerception;

        private final int count;

        public static Averager accept(Averager averager, HourlyWeather weather) {

            long timestamp = averager.timestamp + weather.getTimestamp();
            double temperature = averager.temperature + weather.getTemperature();
            double dewPoint = averager.dewPoint + weather.getDewPoint();
            double humidity = averager.humidity + weather.getHumidity();
            double pressure = averager.pressure + weather.getPressure();
            double wind = averager.wind + weather.getWind();
            double visibility = averager.visibility + weather.getVisibility();
            double clouds = averager.clouds + weather.getClouds();
            double perception = averager.perception + weather.getPerception();

            long maxTimestamp = averager.maxTimestamp > weather.getTimestamp() && averager.count > 0 ? averager.maxTimestamp : weather.getTimestamp();
            double maxTemperature = averager.maxTemperature > weather.getTemperature()  && averager.count > 0 ? averager.maxTemperature : weather.getTemperature();
            double maxDewPoint = averager.maxDewPoint > weather.getDewPoint()  && averager.count > 0 ? averager.maxDewPoint : weather.getDewPoint();
            double maxHumidity = averager.maxHumidity > weather.getHumidity()  && averager.count > 0 ? averager.maxHumidity : weather.getHumidity();
            double maxPressure = averager.maxPressure > weather.getPressure()  && averager.count > 0 ? averager.maxPressure : weather.getPressure();
            double maxWind = averager.maxWind > weather.getWind()  && averager.count > 0 ? averager.maxWind : weather.getWind();
            double maxVisibility = averager.maxVisibility > weather.getVisibility()  && averager.count > 0 ? averager.maxVisibility : weather.getVisibility();
            double maxClouds = averager.maxClouds > weather.getClouds()  && averager.count > 0 ? averager.maxClouds : weather.getClouds();
            double maxPerception = averager.maxPerception > weather.getPerception()  && averager.count > 0 ? averager.maxPerception : weather.getPerception();

            long minTimestamp = averager.minTimestamp < weather.getTimestamp()  && averager.count > 0 ? averager.minTimestamp : weather.getTimestamp();
            double minTemperature = averager.minTemperature < weather.getTemperature() && averager.count > 0 ? averager.minTemperature : weather.getTemperature();
            double minDewPoint = averager.minDewPoint < weather.getDewPoint()  && averager.count > 0 ? averager.minDewPoint : weather.getDewPoint();
            double minHumidity = averager.minHumidity < weather.getHumidity()  && averager.count > 0 ? averager.minHumidity : weather.getHumidity();
            double minPressure = averager.minPressure < weather.getPressure()  && averager.count > 0 ? averager.minPressure : weather.getPressure();
            double minWind = averager.minWind < weather.getWind()  && averager.count > 0 ? averager.minWind : weather.getWind();
            double minVisibility = averager.minVisibility < weather.getVisibility()  && averager.count > 0 ? averager.minVisibility : weather.getVisibility();
            double minClouds = averager.minClouds < weather.getClouds()  && averager.count > 0 ? averager.minClouds : weather.getClouds();
            double minPerception = averager.minPerception < weather.getPerception()  && averager.count > 0 ? averager.minPerception : weather.getPerception();

            int count = averager.count + 1;

            return new Averager(timestamp, temperature, dewPoint, humidity, pressure, wind, visibility, clouds, perception,
                    maxTimestamp, maxTemperature, maxDewPoint, maxHumidity, maxPressure, maxWind, maxVisibility, maxClouds, maxPerception,
                    minTimestamp, minTemperature, minDewPoint, minHumidity, minPressure, minWind, minVisibility, minClouds, minPerception,
                    count);
        }

        public static Averager accept(Averager averager1, Averager averager2) {
            long timestamp = averager1.timestamp + averager2.timestamp;
            double temperature = averager1.temperature + averager2.temperature;
            double dewPoint = averager1.dewPoint + averager2.dewPoint;
            double humidity = averager1.humidity + averager2.humidity;
            double pressure = averager1.pressure + averager2.pressure;
            double wind = averager1.wind + averager2.wind;
            double visibility = averager1.visibility + averager2.visibility;
            double clouds = averager1.clouds + averager2.clouds;
            double perception = averager1.perception + averager2.getPerception();

            long maxTimestamp = averager1.maxTimestamp > averager2.maxTimestamp ? averager1.maxTimestamp : averager2.maxTimestamp;
            double maxTemperature = averager1.maxTemperature > averager2.maxTemperature ? averager1.maxTemperature : averager2.maxTemperature;
            double maxDewPoint = averager1.maxDewPoint > averager2.maxDewPoint ? averager1.maxDewPoint : averager2.maxDewPoint;
            double maxHumidity = averager1.maxHumidity > averager2.maxHumidity ? averager1.maxHumidity : averager2.maxHumidity;
            double maxPressure = averager1.maxPressure > averager2.maxPressure ? averager1.maxPressure : averager2.maxPressure;
            double maxWind = averager1.maxWind > averager2.maxWind ? averager1.maxWind : averager2.maxWind;
            double maxVisibility = averager1.maxVisibility > averager2.maxVisibility ? averager1.maxVisibility : averager2.maxVisibility;
            double maxClouds = averager1.maxClouds > averager2.maxClouds ? averager1.maxClouds : averager2.maxClouds;
            double maxPerception = averager1.maxPerception > averager2.getPerception() ? averager1.maxPerception : averager2.getPerception();

            long minTimestamp = averager1.minTimestamp < averager2.minTimestamp ? averager1.minTimestamp : averager2.minTimestamp;
            double minTemperature = averager1.minTemperature < averager2.minTemperature ? averager1.minTemperature : averager2.minTemperature;
            double minDewPoint = averager1.minDewPoint < averager2.minDewPoint ? averager1.minDewPoint : averager2.minDewPoint;
            double minHumidity = averager1.minHumidity < averager2.minHumidity ? averager1.minHumidity : averager2.minHumidity;
            double minPressure = averager1.minPressure < averager2.minPressure ? averager1.minPressure : averager2.minPressure;
            double minWind = averager1.minWind < averager2.minWind ? averager1.minWind : averager2.minWind;
            double minVisibility = averager1.minVisibility < averager2.minVisibility ? averager1.minVisibility : averager2.minVisibility;
            double minClouds = averager1.minClouds < averager2.minClouds ? averager1.minClouds : averager2.minClouds;
            double minPerception = averager1.minPerception < averager2.getPerception() ? averager1.minPerception : averager2.getPerception();

            int count = averager1.count + averager2.count;

            return new Averager(timestamp, temperature, dewPoint, humidity, pressure, wind, visibility, clouds, perception,
                    maxTimestamp, maxTemperature, maxDewPoint, maxHumidity, maxPressure, maxWind, maxVisibility, maxClouds, maxPerception,
                    minTimestamp, minTemperature, minDewPoint, minHumidity, minPressure, minWind, minVisibility, minClouds, minPerception,
                    count);
        }

        public HourlyWeather get() {
            return new HourlyWeather(timestamp / count, temperature / count, dewPoint / count, humidity / count, pressure / count, wind / count, visibility / count, clouds / count, perception / count);
        }

        public static Averager init() {
            return new Averager(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

}
