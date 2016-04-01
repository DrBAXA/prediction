package com.vdanyliuk.data.weather.forecast.astronomical;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.util.ParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AstronomicalDataProvider extends Cache<LocalDate, AstronomyData> {

    private static final long serialVersionUID = 1L;

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final static String DEFAULT_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";

    @Override
    protected AstronomyData getNonCachedData(LocalDate date) {
        return getWeather(getWeatherDayPage(date));
    }

    private Document getWeatherDayPage(LocalDate date) {
        return ParserUtil.getDocument(getUrlWithDate(DEFAULT_URL, date), 0);
    }

    private AstronomyData getWeather(Document document) {
        return new AstronomicalDataExtractor(document).getData();
    }

    private String getUrlWithDate(String urlPattern, LocalDate date) {
        return urlPattern.replace("${Date}", DATE_FORMATTER.format(date));
    }
}
