package com.vdanyliuk.weather;

import org.jsoup.nodes.Document;

import static com.vdanyliuk.util.ParserUtil.getMinutesValueForCssAndRegex;
import static com.vdanyliuk.util.ParserUtil.getValueForCssAndRegex;

public class ForecastWeatherDataExtractor {

    private Document document;

    public ForecastWeatherDataExtractor(Document document) {
        this.document = document;
    }

    public WeatherModel getWeather() {
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
                .build();
    }

    double getAstronomicalDayLong() {
        return getMinutesValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість дня\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

    double getDayLightLong() {
        return getMinutesValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість видимого світла\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

    double getAvgHumidity() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Average Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMaxHumidity() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Maximum Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMinHumidity() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Minimum Humidity\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getAvgTemperature() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Середня Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMaxTemperature() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Максимальна Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getMinTemperature() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Мінімальна Температура\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getDegreeDays() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Індекс нагріву \\(HDD\\)\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getDewPoint() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Точка Роси\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getPrecipitation() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Опади\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getPressure() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Атмосферний Тиск\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getWind() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Швидкість Вітру\\s*(\\-?\\d+\\.?\\d*)");
    }

    double getVisibility() {
        return getValueForCssAndRegex(document, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Видимість\\s*(\\-?\\d+\\.?\\d*)");
    }

}
