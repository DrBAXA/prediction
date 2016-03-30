package com.vdanyliuk.data.weather.forecast.astronomical;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.util.ParserUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class StoredAstronomicalDataProvider extends Cache<LocalDate, AstronomyData> implements AstronomicalDataProvider {

    private static final String URL = "http://ua.365.wiki/world/ukraine/ivano-frankivsk/sun/today/";

    @Override
    protected AstronomyData getNonCachedData(LocalDate key) {
        return getFromNet(key);
    }

    AstronomyData getFromNet(LocalDate date) {

        String monthDay = date.format(DateTimeFormatter.ofPattern("d MMMM"));

        Document document = ParserUtil.getDocument(URL);

        assert document != null;
        Element dayElement = document.select("div#w3 div.overflow-auto table.table.table-bordered.table-sun tbody tr td > time:containsOwn(" + monthDay + ")").get(0).parent().parent();

        AstronomyData data = AstronomyData.builder()
                .astronomicalDayLong(getAstronomicalDayLong(dayElement))
                .dayLightLong(getDayLightLong(dayElement))
                .sunRise(getSunRise(dayElement, date))
                .sunSet(getSunSet(dayElement, date))
                .build();

        return data;

    }

    LocalTime getSunRise(Element element, LocalDate date) {
        ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(2)", "(.*)");
        return ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(2)", "(.*)").plusHours((long) DateBasedWeatherDataModel.isDayLightSaving(date));
    }

    LocalTime getSunSet(Element element, LocalDate date) {
        return ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(4)", "(.*)").plusHours((long) DateBasedWeatherDataModel.isDayLightSaving(date));
    }

    double getAstronomicalDayLong(Element element) {
        return ChronoUnit.MINUTES.between(LocalTime.of(0, 0), ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(5)", "(.*)\\+"));
    }

    double getDayLightLong(Element element) {
        return ChronoUnit.MINUTES.between(ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(10)", "(.*)"), ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(11)", "(.*)"));
    }


}
