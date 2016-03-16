package com.vdanyliuk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Convert {

    public static final DateTimeFormatter F1 = DateTimeFormatter.ofPattern("yyyy.M.d");
    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final Map<LocalDate, Double> STATE_HOLIDAYS = new HashMap<LocalDate, Double>(){{
        put(LocalDate.of(2015, 1, 1), 1.0);
        put(LocalDate.of(2015, 1, 7), 1.0);
        put(LocalDate.of(2015, 3, 9), 1.0);
        put(LocalDate.of(2015, 4, 13), 1.0);
        put(LocalDate.of(2015, 5, 1), 1.0);
        put(LocalDate.of(2015, 5, 4), 1.0);
        put(LocalDate.of(2015, 5, 11), 1.0);
        put(LocalDate.of(2015, 6, 29), 1.0);
        put(LocalDate.of(2015, 8, 24), 1.0);
        put(LocalDate.of(2015, 10, 14), 1.0);
        put(LocalDate.of(2016, 1, 1), 1.0);
        put(LocalDate.of(2016, 1, 7), 1.0);
        put(LocalDate.of(2016, 3, 8), 1.0);
    }};

    public static final Map<LocalDate, Double> RELIGIOUS_HOLIDAYS = new HashMap<LocalDate, Double>(){{
        put(LocalDate.of(2015, 1, 6), 1.0);
        put(LocalDate.of(2015, 1, 7), 1.0);
        put(LocalDate.of(2015, 1, 8), 1.0);
        put(LocalDate.of(2015, 1, 9), 1.0);
        put(LocalDate.of(2015, 1, 14), 1.0);
        put(LocalDate.of(2015, 1, 19), 1.0);
        put(LocalDate.of(2015, 1, 20), 1.0);
        put(LocalDate.of(2015, 1, 18), 1.0);
        put(LocalDate.of(2015, 2, 12), 1.0);
        put(LocalDate.of(2015, 2, 15), 1.0);
        put(LocalDate.of(2015, 4, 7), 1.0);
        put(LocalDate.of(2015, 4, 10), 1.0);
        put(LocalDate.of(2015, 4, 12), 1.0);
        put(LocalDate.of(2015, 4, 13), 1.0);
        put(LocalDate.of(2015, 4, 14), 1.0);
        put(LocalDate.of(2015, 5, 6), 1.0);
        put(LocalDate.of(2015, 5, 21), 1.0);
        put(LocalDate.of(2015, 5, 21), 1.0);
        put(LocalDate.of(2015, 7, 7), 1.0);
        put(LocalDate.of(2015, 7, 12), 1.0);
        put(LocalDate.of(2015, 8, 2), 1.0);
        put(LocalDate.of(2015, 8, 19), 1.0);
        put(LocalDate.of(2015, 8, 28), 1.0);
        put(LocalDate.of(2015, 9, 21), 1.0);
        put(LocalDate.of(2015, 9, 27), 1.0);
        put(LocalDate.of(2015, 11, 21), 1.0);
        put(LocalDate.of(2015, 12, 13), 1.0);
        put(LocalDate.of(2015, 12, 19), 1.0);
        put(LocalDate.of(2015, 12, 25), 1.0);
        put(LocalDate.of(2016, 1, 6), 1.0);
        put(LocalDate.of(2016, 1, 7), 1.0);
        put(LocalDate.of(2016, 1, 8), 1.0);
        put(LocalDate.of(2016, 1, 9), 1.0);
        put(LocalDate.of(2016, 1, 14), 1.0);
        put(LocalDate.of(2016, 1, 19), 1.0);
        put(LocalDate.of(2016, 1, 20), 1.0);
        put(LocalDate.of(2016, 1, 18), 1.0);
        put(LocalDate.of(2016, 2, 12), 1.0);
        put(LocalDate.of(2016, 2, 15), 1.0);
    }};

    public static void main(String[] args) throws IOException {
        Map<LocalDate, Integer> load = Files.lines(Paths.get("load.csv"))
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, Integer>::new, (m, a) -> m.put(LocalDate.parse(a[0], F2), Integer.valueOf(a[1])), HashMap::putAll);


        Map<LocalDate, Average> clouds = new HashMap<>();
        Files.lines(Paths.get("w.csv"))
                .map(l -> l.split(";"))
                .forEach(a -> clouds.compute(LocalDate.parse(a[0], F2), (d, av) -> av == null ? new Average() : av.add(Double.parseDouble(a[1]))));

        Map<LocalDate, String> wether = normalizeWeather();

        Files.write(Paths.get("out.csv.temp"), wether.keySet().stream().sorted()
                .filter(load::containsKey)
                .map(d -> load.get(d) + "," + clouds.get(d) + "," + getREloyHolidays(d) + "," + getDayOfWeekValue(d) + "," + isDayLightSaving(d) + "," +wether.get(d))
                .collect(Collectors.toList()));

        Files.write(Paths.get("out.csv"), Files.lines(Paths.get("out.csv.temp"))
                .map(l -> l.split(","))
                .map(a -> String.join(",", a) + "," + tripleTemp(a) + "," + addSquare(a) + "," + -(Double.parseDouble(a[3]))*Double.parseDouble(a[16]))
                .collect(Collectors.toList()));
    }

    private static String addSquare(String[] a){
        return Stream.of(a).skip(1).mapToDouble(Double::valueOf).map(d -> Math.abs(d)*d).mapToObj(d -> ""+d).collect(Collectors.joining(","));
    }

    private static String tripleTemp(String[] a) {
        return Stream.of(a[3]).skip(1).mapToDouble(Double::valueOf).map(d -> Math.pow(d, 3)).mapToObj(d -> "" + d).collect(Collectors.joining(","));
    }

    private static class Average {
        private double val;
        private int count;
        public Average add(double d){
            val += d;
            count++;
            return this;
        }
        public double get(){
            return val/count;
        }

        @Override
        public String toString() {
            return "" + get();
        }
    }

    public static double getREloyHolidays(LocalDate date) {
        if(date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) return 0;
        return Optional.ofNullable(RELIGIOUS_HOLIDAYS.get(date)).map(d -> d*-80).orElse(0.0);
    }

    public static double isDayLightSaving(LocalDate date) {
        return ZoneId.systemDefault().getRules().isDaylightSavings(Instant.from(date.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC))) ? 1 : 0;
    }

    public static double getDayOfWeekValue(LocalDate localDate){
        if(STATE_HOLIDAYS.containsKey(localDate)) return -100;
        switch (localDate.getDayOfWeek()){
            case MONDAY: return 0;
            case TUESDAY: return 0;
            case WEDNESDAY:return 0;
            case THURSDAY:return 0;
            case FRIDAY:return 0;
            case SATURDAY:return 0;
            case SUNDAY:return -100;
            default:return 0;
        }
    }

    public static Map<LocalDate, String> normalizeWeather() throws IOException {
        return Files.lines(Paths.get("weather.csv"))
                .skip(1)
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, String>::new, (m, a) -> m.put(LocalDate.parse(a[0], F1), String.join(",", Arrays.copyOfRange(a, 1, a.length))), HashMap::putAll);
    }

}
