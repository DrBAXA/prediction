package com.vdanyliuk.data.weather;

import java.time.LocalDate;
import java.util.List;

public interface WeatherParser {

    List<WeatherModel> getWeather(LocalDate date, LocalDate date1);
}