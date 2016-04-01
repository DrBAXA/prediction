package com.vdanyliuk.data.weather.historical;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.weather.WeatherDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.util.ParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class HistoricalWeatherDataProvider extends Cache<LocalDate, WeatherModel> implements WeatherDataProvider {

    private static final long serialVersionUID = 1L;

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final static String DEFAULT_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";

    private DataProvider<Double> clouds;


    protected HistoricalWeatherDataProvider(DataProvider<Double> clouds) throws IOException {
        this.clouds = clouds;
    }


    @Override
    protected WeatherModel getNonCachedData(LocalDate date) {
        log.info("Loading data for " + date.toString());
        return getWeather(getWeatherDayPage(date), date);
    }


    Document getWeatherDayPage(LocalDate date) {
        return ParserUtil.getDocument(getUrlWithDate(DEFAULT_URL, date), 0);
    }

    WeatherModel getWeather(Document document, LocalDate date) {
        return new HistoryWeatherDataExtractor(document, date).getWeather(clouds);
    }

    String getUrlWithDate(String urlPattern, LocalDate date) {
        return urlPattern.replace("${Date}", DATE_FORMATTER.format(date));
    }
}
