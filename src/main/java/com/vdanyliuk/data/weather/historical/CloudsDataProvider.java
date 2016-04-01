package com.vdanyliuk.data.weather.historical;

import com.vdanyliuk.data.Cache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class CloudsDataProvider extends Cache<LocalDate, Double> {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Pattern PATTERN = Pattern.compile("href=(.+gz)>");
    private static final int CLOUDS_COLUMN_NUMBER = 10;
    private static final int DATE_COLUMN_NUMBER = 0;

    @Override
    protected Double getNonCachedData(LocalDate key) {
        if(key.isAfter(LocalDate.now()) || key.equals(LocalDate.now())) throw new RuntimeException("Can't get data from today or future.");
        Map<LocalDate, Double> res =  getLines(getGZipContent(getArchiveLink(getInitialDocument(key))))
                .stream()
                .skip(7)
                .map(s -> s.replaceAll("\"", ""))
                .map(s -> s.split(";"))
                .map(a -> new KeyVal(INPUT_DATE_FORMATTER.parse(a[DATE_COLUMN_NUMBER], TemporalQueries.localDate()), parse(a[CLOUDS_COLUMN_NUMBER])))
                .collect(Collectors.groupingBy(KeyVal::getKey, Collectors.averagingDouble(KeyVal::getVal)));

        cache.putAll(res);

        return res.get(key);
    }

    private List<String> getLines(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Can't read from gzipped response");
        }
    }

    private InputStream getGZipContent(String url) {
        try {
            return new GZIPInputStream(new URL(url).openStream());
        } catch (IOException e) {
            throw new RuntimeException("Can't open URL " + url);
        }
    }

    private String getArchiveLink(Document document) {
        Matcher matcher = PATTERN.matcher(document.toString());
        if(matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Cant get file ul from response " + document);
        }
    }

    private Document getInitialDocument(LocalDate date) {
        try {
            return Jsoup
                    .connect("http://rp5.ua/inc/f_archive.php")
                    .data("a_date1", FORMATTER.format(date.minusDays(2)))
                    .data("a_date2", FORMATTER.format(LocalDate.now()))
                    .data("f_ed3", "3")
                    .data("f_ed4", "3")
                    .data("f_ed5", "31")
                    .data("f_pe", "1")
                    .data("f_pe1", "2")
                    .data("lng_id", "3")
                    .data("wmo_id", "33526")
                    .post();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Double parse(String s) {
        Pattern pattern = Pattern.compile("\\d+\\.?\\d+");
        Matcher matcher = pattern.matcher(s);
        if(matcher.find()) {
            return Double.valueOf(matcher.group());
        }

        if(s.matches("Небо не видно.*")) return 100.0;
        if(s.matches(".*хмар немає.*")) return 0.0;


        throw new RuntimeException("can't get number from string " + s);
    }

    @AllArgsConstructor
    private static class KeyVal {
        @Getter
        private final LocalDate key;
        @Getter
        private final Double val;
    }

}
