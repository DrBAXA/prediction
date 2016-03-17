package com.vdanyliuk;

import com.vdanyliuk.util.Average;
import com.vdanyliuk.weather.WUndergroundWeatherParser;
import com.vdanyliuk.weather.WeatherModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Main {
    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static UnaryOperator<Double> same = d -> d;
    private static UnaryOperator<Double> signedSquare = d -> Math.abs(d)*d;

    public static void main(String[] args) throws IOException {
        prepareFiles();
    }

    public static void prepareFiles() throws IOException {
        WUndergroundWeatherParser parser = new WUndergroundWeatherParser(getCloudsData());

        Map<LocalDate, Double> load = getLoad();
        Holidays holidays = new Holidays();

        Files.write(Paths.get("out.csv"), parser.getWeather(LocalDate.of(2015,3,1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getMatrixRowString(wr, holidays, load))
                .collect(Collectors.toList()));

        double[][] xArray = parser.getWeather(LocalDate.of(2015,3,1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getXMatrixRowArray(wr, holidays, load))
                .collect(Collectors.toList())
                .toArray(new double[1][1]);

        double[][] yArray = parser.getWeather(LocalDate.of(2015,3,1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getYMatrixRowArray(wr, holidays, load))
                .collect(Collectors.toList())
                .toArray(new double[1][1]);

        RealMatrix X = new Array2DRowRealMatrix(xArray);
        RealMatrix y = new Array2DRowRealMatrix(yArray);

        RealMatrix theta = new LUDecomposition((X.transpose().multiply(X))).getSolver().getInverse().multiply(X.transpose()).multiply(y);
        RealMatrix dy = y.add(X.multiply(theta).scalarMultiply(-1));
        System.out.println("==============J========================");
        System.out.println(DoubleStream.of(dy.getColumn(0)).map(d -> d*d).sum());
        System.out.println("=======================================");
    }

    private static String getMatrixRowString(WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return getDataBuilder(model, holidays, load).build();
    }

    private static double[] getXMatrixRowArray(WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return getDataBuilder(model, holidays, load).buildXArrayRow();
    }

    private static double[] getYMatrixRowArray(WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return new double[]{load.get(model.getDate())};
    }

    public static MatrixBuilder getDataBuilder(WeatherModel model, Holidays holidays, Map<LocalDate, Double> load){
        return new MatrixBuilder(load.get(model.getDate()))
                //Holidays
                .addParameter(holidays.religious(model.getDate()), d -> d*-80)
                .addParameter(holidays.state(model.getDate()), d -> d*-100)

                //Regular weather parameters
                .addParameter(model.getAstronomicalDayLong(), same)
                .addParameter(model.getAvgHumidity(), same)
                .addParameter(model.getAvgTemperature(), same)
                .addParameter(model.getClouds(), same)
                .addParameter(model.getDayLightLong(), same)
                .addParameter(model.getDewPoint(), same)
                .addParameter(model.getMaxHumidity(), same)
                .addParameter(model.getMaxTemperature(), same)
                .addParameter(model.getMinHumidity(), same)
                .addParameter(model.getMinTemperature(), same)
                .addParameter(model.getPrecipitation(), same)
                .addParameter(model.getPressure(), same)
                .addParameter(model.getSunRiseBeforeWork(), same)
                .addParameter(model.getSunSetBeforeWork(), same)
                .addParameter(model.getVisibility(), same)
                .addParameter(model.getWind(), same)

                //Square weather parameters
                .addParameter(model.getAstronomicalDayLong(), signedSquare)
                .addParameter(model.getAvgHumidity(), signedSquare)
                .addParameter(model.getAvgTemperature(), signedSquare)
                .addParameter(model.getClouds(), signedSquare)
                .addParameter(model.getDayLightLong(), signedSquare)
                .addParameter(model.getDewPoint(), signedSquare)
                //.addParameter(model.getMaxHumidity(), signedSquare)
                //.addParameter(model.getMaxTemperature(), signedSquare)
                //.addParameter(model.getMinHumidity(), signedSquare)
                //.addParameter(model.getMinTemperature(), signedSquare)
                .addParameter(model.getPrecipitation(), signedSquare)
                .addParameter(model.getPressure(), signedSquare)
                .addParameter(model.getVisibility(), signedSquare)
                .addParameter(model.getWind(), signedSquare)

                //Special parameters
                .addParameter(isDayLightSaving(model.getDate()), same)
                .addParameter(model.getAvgTemperature(), d -> Math.pow(d, 3))
                .addParameter(model.getAvgTemperature()* holidays.state(model.getDate()), d -> -d);
    }

    public static double isDayLightSaving(LocalDate date) {
        return ZoneId.systemDefault().getRules().isDaylightSavings(Instant.from(date.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC))) ? 1 : 0;
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

    private static Map<LocalDate, Double> getLoad() throws IOException {
        return Files.lines(Paths.get("data/load.csv"))
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, Double>::new, (m, a) -> m.put(LocalDate.parse(a[0], F2), Double.valueOf(a[1])), HashMap::putAll);

    }

}
