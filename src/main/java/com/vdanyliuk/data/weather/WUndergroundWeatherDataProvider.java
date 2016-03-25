package com.vdanyliuk.data.weather;

import com.vdanyliuk.util.Average;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class WUndergroundWeatherDataProvider implements WeatherDataProvider {

    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final static String DEFAULT_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";

    private Map<LocalDate, Double> clouds;
    Map<LocalDate, WeatherModel> cache;

    public WUndergroundWeatherDataProvider() throws IOException {
        this.clouds = getCloudsData();
        cache = loadCache();
    }

    @Override
    public WeatherModel getData(LocalDate date) {
        return Optional.ofNullable(cache.get(date))
                .orElseGet(() ->getWeather(getWeatherDayPage(date), date));
    }

    public List<WeatherModel> getWeather(LocalDate startDate, LocalDate endDate) {
        List<WeatherModel> res = Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate))
                .filter(d -> clouds.containsKey(d))
                .map(d -> Optional.ofNullable(cache.get(d))
                        .orElseGet(() -> getWeather(getWeatherDayPage(d), d)))
                .collect(Collectors.toList());

        saveCache(res.stream().collect(HashMap::new, (m, wm) -> m.put(wm.getDate(), wm), Map::putAll));

        return res;
    }

    private void saveCache(Map<LocalDate, WeatherModel> cache) {
        try (OutputStream outputStream = new FileOutputStream("data/cache.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            this.cache.putAll(cache);
            objectOutputStream.writeObject(this.cache);

        } catch (IOException e) {
            log.error("Can't save cache.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<LocalDate, WeatherModel> loadCache() {
        try (InputStream inputStream = new FileInputStream("data/cache.dat");
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            Map<LocalDate, WeatherModel> res = (Map<LocalDate, WeatherModel>) objectInputStream.readObject();
            return Optional.ofNullable(res).orElseGet(HashMap::new);

        } catch (IOException | ClassNotFoundException e) {
            log.error("Can't load cache.");
            return new HashMap<>();
        }
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
