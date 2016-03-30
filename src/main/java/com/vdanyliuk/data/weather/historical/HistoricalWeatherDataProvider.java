package com.vdanyliuk.data.weather.historical;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.weather.WeatherDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.util.Average;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HistoricalWeatherDataProvider extends Cache<LocalDate, WeatherModel> implements WeatherDataProvider {

    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final static String DEFAULT_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";

    private Map<LocalDate, Double> clouds;


    protected HistoricalWeatherDataProvider() throws IOException {
        clouds = getCloudsData();
    }

    @Override
    protected WeatherModel getNonCachedData(LocalDate date) {
        log.info("Loading data for " + date.toString());
        return getWeather(getWeatherDayPage(date), date);
    }


    Document getWeatherDayPage(LocalDate date) {
        return getWeatherDayPage(date, 0);
    }

    WeatherModel getWeather(Document document, LocalDate date) {
        return new HistoryWeatherDataExtractor(document, date).getWeather(clouds);
    }

    private Document getWeatherDayPage(LocalDate date, int tryCount) {
        try {
            return getConnection(date).get();
        } catch (IOException e) {
            if (tryCount < 5) {
                log.warn("Can't get page. Try to get one more time");
                return getWeatherDayPage(date, tryCount + 1);
            } else throw new RuntimeException("Can't load data from internet");
        }
    }

    Connection getConnection(LocalDate date) {
        String url = getUrlWithDate(DEFAULT_URL, date);
        return Jsoup.connect(url);
    }

    String getUrlWithDate(String urlPattern, LocalDate date) {
        return urlPattern.replace("${Date}", DATE_FORMATTER.format(date));
    }

    private static Map<LocalDate, Double> getCloudsData() throws IOException {
        Map<LocalDate, Average> clouds = new HashMap<>();
        Files.lines(Paths.get("data/clouds.csv"))
                .map(l -> l.split(";"))
                .forEach(a -> clouds.compute(LocalDate.parse(a[0], F2),
                        (d, av) -> av == null ? new Average() : av.add(Double.parseDouble(a[1]))));

        return clouds.entrySet()
                .stream()
                .collect(HashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue().get()),
                        HashMap::putAll);
    }
}
