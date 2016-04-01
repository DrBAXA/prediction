package com.vdanyliuk.data.weather.forecast.astronomical;

import org.jsoup.nodes.Document;

import java.time.LocalTime;

import static com.vdanyliuk.util.ParserUtil.getMinutesValueForCssAndRegex;
import static com.vdanyliuk.util.ParserUtil.getTimeValueForCssAndRegex;

public class AstronomicalDataExtractor {

    private Document document;

    public AstronomicalDataExtractor(Document document) {
        this.document = document;
    }

    public AstronomyData getData() {
        return AstronomyData.builder()
                .astronomicalDayLong(getAstronomicalDayLong())
                .dayLightLong(getDayLightLong())
                .sunRise(getSunRise())
                .sunSet(getSunSet())
                .build();
    }

    LocalTime getSunRise() {
        return getTimeValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Фактичний час\\s+(\\d{2}:\\d{2})");
    }

    LocalTime getSunSet() {
        return getTimeValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Фактичний час.*\\s+(\\d{2}:\\d{2})\\s+EES?T Цивільні Сутінки");
    }

    double getAstronomicalDayLong() {
        return getMinutesValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість дня\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

    double getDayLightLong() {
        return getMinutesValueForCssAndRegex(document, "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість видимого світла\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m");
    }

}
