package com.vdanyliuk.weather;

import com.vdanyliuk.util.PropertiesUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.stream.Stream;

@Slf4j
public class WUndergroundWeatherParser implements WeatherParser {

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final static String DEFAULT_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";

    private Writer writer;

    public static void main(String[] args) throws IOException {
        WUndergroundWeatherParser parser = new WUndergroundWeatherParser();
        LocalDate date = LocalDate.of(2016, 3, 1);
        parser.writer = getFileWriter();
        Stream.iterate(date, d -> d.plusDays(1))
                .limit(16)
                .peek(d -> log.info("Date " + d + " done."))
                .map(parser::getWeather)
                .map(WeatherModel::toString)
                .forEach(parser::write);

        parser.writer.close();
    }

    private void write(String s){
        try {
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Writer getFileWriter() {
        try {
            return new FileWriter("out.csv");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Getter
    private Properties properties;

    public WUndergroundWeatherParser() {
        properties = PropertiesUtil.loadProperties("wunderground.properties");
    }

    public WeatherModel getWeather(LocalDate date) {
        return getWeather(getWeatherDayPage(date), date);
    }

    Document getWeatherDayPage(LocalDate date) {
        return getWeatherDayPage(date, 0);
    }

    WeatherModel getWeather(Document document, LocalDate date) {
        return new HistoryWeatherDataExtractor(document, date).getWeather();
    }

    private Document getWeatherDayPage(LocalDate date, int tryCount) {
        try {
            return getConnection(date).get();
        } catch (IOException e) {
            if(tryCount < Integer.parseInt(properties.getProperty("connection.maxTryCount", "5"))){
                log.warn("Can't get page. Try to get one more time");
                return getWeatherDayPage(date, tryCount+1);
            }
            else throw new RuntimeException("Can't load data from internet");
        }
    }

    Connection getConnection(LocalDate date) {
        String url = getUrlWithDate(properties.getProperty("connection.url", DEFAULT_URL), date);
        return Jsoup.connect(url);
    }

    String getUrlWithDate(String urlPattern, LocalDate date) {
        return urlPattern.replace("${Date}", DATE_FORMATTER.format(date));
    }

}
