package com.vdanyliuk.data.astronomical;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.util.ParserUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StoredAstronomicalDataProvider extends Cache implements AstronomicalDataProvider{

    private static final String URL = "http://ua.365.wiki/world/ukraine/ivano-frankivsk/sun/today/";

    private Map<LocalDate, AstronomyData> cache;

    public StoredAstronomicalDataProvider() {
        cache = new HashMap<>();
    }

    @Override
    public AstronomyData getData(LocalDate date) {
        return Optional.ofNullable(cache.get(date)).orElseGet(() -> getFromNet(date));
    }

    AstronomyData getFromNet(LocalDate date) {
        checkIfCanGet(date);

        String monthDay = date.format(DateTimeFormatter.ofPattern("d MMMM"));

        Document document = ParserUtil.getDocument(URL);

        assert document != null;
        Element dayElement = document.select("div#w3 div.overflow-auto table.table.table-bordered.table-sun tbody tr td > time:containsOwn(" + monthDay + ")").get(0).parent().parent();

        AstronomyData data = AstronomyData.builder()
                .astronomicalDayLong(getAstronomicalDayLong(dayElement))
                .dayLightLong(getDayLightLong(dayElement))
                .sunRise(getSunRise(dayElement))
                .sunSet(getSunSet(dayElement))
                .build();

        cache.put(date, data);
        return data;

    }

    LocalTime getSunRise(Element element) {
        return ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(2)", "(.*)");
    }

    LocalTime getSunSet(Element element) {
        return ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(4)", "(.*)");
    }

    double getAstronomicalDayLong(Element element) {
        return ChronoUnit.MINUTES.between(LocalTime.of(0,0), ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(5)", "(.*)\\+"));
    }

    double getDayLightLong(Element element) {
        return ChronoUnit.MINUTES.between(ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(6)", "(.*)"), ParserUtil.getTimeValueForCssAndRegex(element, "td:eq(7)", "(.*)"));
    }

    void checkIfCanGet(LocalDate date) {
        long diff = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if(diff < -1 || diff > 10) throw new RuntimeException();
    }

}
