package com.vdanyliuk.weather;

import java.time.LocalDate;

public interface WeatherParser {

    WeatherModel getWeather(LocalDate date);
}
