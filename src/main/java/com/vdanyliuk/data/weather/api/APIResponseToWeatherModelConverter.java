package com.vdanyliuk.data.weather.api;

import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.astronomical.AstronomyData;
import com.vdanyliuk.data.weather.WeatherModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.function.UnaryOperator;

public class APIResponseToWeatherModelConverter {

    private DataProvider<AstronomyData> astronomyDataDataProvider;

    private UnaryOperator<Double> FARENGATE_TO_CELSIUS = f -> (f-32)/1.8;

    public APIResponseToWeatherModelConverter(DataProvider<AstronomyData> astronomyDataDataProvider) {
        this.astronomyDataDataProvider = astronomyDataDataProvider;
    }

    public WeatherModel convert(Collection<HourlyWeather> hourlyWeather) {
        HourlyWeather.Averager averager = hourlyWeather.stream()
                .reduce(HourlyWeather.Averager.init(), HourlyWeather.Averager::accept, HourlyWeather.Averager::accept);

        HourlyWeather weather = averager.get();

        LocalDate date = LocalDateTime.ofEpochSecond(weather.getTimestamp(), 0, ZoneOffset.ofHours(2)).toLocalDate();

        AstronomyData astronomyData = astronomyDataDataProvider.getData(date);

        return WeatherModel.builder()

                .date(date)

                .avgTemperature(FARENGATE_TO_CELSIUS.apply(weather.getTemperature()))
                .minTemperature(FARENGATE_TO_CELSIUS.apply(averager.getMinTemperature()))
                .maxTemperature(FARENGATE_TO_CELSIUS.apply(averager.getMaxTemperature()))

                .avgHumidity(weather.getHumidity()*100)
                .minHumidity(averager.getMinHumidity()*100)
                .maxHumidity(averager.getMaxHumidity()*100)

                .clouds(weather.getClouds()*100)
                .dewPoint(weather.getDewPoint())
                .pressure(weather.getPressure())
                .precipitation(weather.getPerception())
                .visibility(weather.getVisibility())
                .wind(weather.getWind())

                .astronomicalDayLong(astronomyData.getAstronomicalDayLong())
                .dayLightLong(astronomyData.getDayLightLong())
                .sunRiseBeforeWork(ChronoUnit.SECONDS.between(astronomyData.getSunRise(), LocalTime.of(6,30)))
                .sunSetBeforeWork(ChronoUnit.SECONDS.between(LocalTime.of(20,0), astronomyData.getSunSet()))

                .build();

    }

}
