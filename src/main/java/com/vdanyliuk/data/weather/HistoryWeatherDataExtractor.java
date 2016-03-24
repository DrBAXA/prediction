package com.vdanyliuk.data.weather;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.vdanyliuk.util.ParserUtil.*;

@Slf4j
public class HistoryWeatherDataExtractor {

    private Document document;
    private LocalDate date;

    public HistoryWeatherDataExtractor(Document document, LocalDate date) {
        this.document = document;
        this.date = date;
    }

    public WeatherModel getWeather(Map<LocalDate, Double> clouds) {
        return WeatherModel.builder()
                .date(date)
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
                .sunRiseBeforeWork(getSunRiseBeforeWork())
                .sunSetBeforeWork(getSunSetBeforeWork())
                .clouds(clouds.get(date))
                .build();
    }

    double getSunRiseBeforeWork() {
        LocalTime sunRise = getTimeValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Фактичний час\\s+(\\d{2}:\\d{2})");
        return ChronoUnit.SECONDS.between(sunRise, LocalTime.of(6,30));
    }

    double getSunSetBeforeWork() {
        LocalTime sunSet = getTimeValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Фактичний час.*\\s+(\\d{2}:\\d{2})\\s+EES?T Цивільні Сутінки");
        return ChronoUnit.SECONDS.between( LocalTime.of(20,0), sunSet);
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
