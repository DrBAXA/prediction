package com.vdanyliuk.data.weather;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;

import static com.vdanyliuk.util.ParserUtil.getMinutesValueForCssAndRegex;
import static com.vdanyliuk.util.ParserUtil.getValueForCssAndRegex;

public class ForecastWeatherDataExtractor {

    private Document document;

    public ForecastWeatherDataExtractor(Document document) {
        this.document = document;
    }

    public WeatherModel getWeather(LocalDate date) {
        return WeatherModel.builder()
                .astronomicalDayLong(getAstronomicalDayLong())
                .dayLightLong(getDayLightLong())
                .avgHumidity(getAvgHumidity())
                .maxHumidity(getMaxHumidity())
                .minHumidity(getMinHumidity())
                .avgTemperature(getAvgTemperature())
                .maxTemperature(getMaxTemperature())
                .minTemperature(getMinTemperature())
                .dewPoint(getDewPoint())
                .precipitation(getPrecipitation())
                .pressure(getPressure())
                .wind(getWind())
                .visibility(getVisibility())
                .clouds(15)
                .build();
    }

    private Element getDayElement(LocalDate localDate) {
        String id = "horizontal-day-" + (localDate.getDayOfYear() - 1);
        return document.select("div#" + id).get(0);
    }

    double getAstronomicalDayLong(LocalDate localDate) {
        return getMinutesValueForCssAndRegex(getDayElement(localDate), "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість дня\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

    double getDayLightLong(LocalDate localDate) {
        return getMinutesValueForCssAndRegex(getDayElement(localDate), "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість видимого світла\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

    double getAvgHumidity(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Average Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMaxHumidity(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Maximum Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMinHumidity(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Minimum Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getAvgTemperature(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Середня Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMaxTemperature(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Максимальна Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMinTemperature(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Мінімальна Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getDewPoint(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Точка Роси\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getPrecipitation(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Опади\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getPressure(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Атмосферний Тиск\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getWind(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Швидкість Вітру\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getVisibility(LocalDate localDate) {
        return getValueForCssAndRegex(getDayElement(localDate), "table#historyTable.responsive.airport-history-summary-table tbody tr", "Видимість\\s*(\\-?\\d+\\.?\\d*)");
    }

}
