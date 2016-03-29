package com.vdanyliuk.data.weather;

import com.vdanyliuk.data.DataProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
public class VisibilityDataProvider implements DataProvider<Double> {
    private static final String URL = "http://www.metoffice.gov.uk/public/weather/forecast/u89qe4pe9#?fcTime=1459296000";

    @Override
    public Double getData(LocalDate date) {
        long dayDif = ChronoUnit.DAYS.between(LocalDate.now(), date);
        return getValue(getDocument(), dayDif);
    }

    private Double getValue(Document document, long dayNumber) {
        return document.select("div.weatherDay" + (dayNumber+1) + " tr.weatherVisibility td").stream()
                .map(e -> e.attr("data-km"))
                .mapToDouble(Double::valueOf)
                .average()
                .orElseThrow(RuntimeException::new);
    }

    private Document getDocument() {
        try {
            return Jsoup.connect(URL).ignoreContentType(true).get();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }
}
