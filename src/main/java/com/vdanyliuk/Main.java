package com.vdanyliuk;

import com.vdanyliuk.util.Average;
import com.vdanyliuk.weather.WUndergroundWeatherParser;
import com.vdanyliuk.weather.WeatherModel;
import org.apache.commons.math3.linear.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Main {
    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static UnaryOperator<Double> same = d -> d;
    private static UnaryOperator<Double> signedSquare = d -> Math.abs(d) * d;
    private static UnaryOperator<Double> cubic = d -> Math.pow(d, 3);
    private static UnaryOperator<Double> sqrt = Math::sqrt;

    public static void main(String[] args) throws IOException {
        prepareFiles();
    }

    public static void prepareFiles() throws IOException {
        WUndergroundWeatherParser parser = new WUndergroundWeatherParser(getCloudsData());

        Map<LocalDate, Double> load = getLoad();
        Holidays holidays = new Holidays();

        Map<LocalDate, WeatherModel> map = parser.getWeather(LocalDate.of(2015, 3, 1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .collect(HashMap::new, (m, wm) -> m.put(wm.getDate(), wm), Map::putAll);

        Files.write(Paths.get("out.csv"), parser.getWeather(LocalDate.of(2015, 3, 1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getMatrixRowString(map, wr, holidays, load))
                .collect(Collectors.toList()));


        double[][] xArray = parser.getWeather(LocalDate.of(2015, 3, 1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getXMatrixRowArray(map, wr, holidays, load))
                .collect(Collectors.toList())
                .toArray(new double[1][1]);

        double[][] yArray = parser.getWeather(LocalDate.of(2015, 3, 1), LocalDate.of(2016, 4, 2)).stream()
                .filter(wr -> load.containsKey(wr.getDate()))
                .map(wr -> getYMatrixRowArray(wr, holidays, load))
                .collect(Collectors.toList())
                .toArray(new double[1][1]);

        RealMatrix X = new Array2DRowRealMatrix(xArray);
        RealMatrix y = new Array2DRowRealMatrix(yArray);

        RealMatrix theta = new LUDecomposition((X.transpose().multiply(X))).getSolver().getInverse().multiply(X.transpose()).multiply(y);
        RealMatrix res = X.multiply(theta);
        RealVector dy = y.add(res.scalarMultiply(-1)).getColumnVector(0);
        RealVector dyp = dy.ebeDivide(y.getColumnVector(0));
        double[] dya = dyp.toArray();
        int allCount = dya.length;
        System.out.println("=======================================");
        System.out.println("squareSum = " + DoubleStream.of(dy.toArray()).map(d -> d * d).sum());
        System.out.println("median = " + DoubleStream.of(dy.toArray()).map(Math::abs).sorted().skip(dy.getDimension()/2).findFirst().orElseGet(() -> 0.0));
        System.out.println("count = " + allCount);
        System.out.printf("max dy = %.2f%n", DoubleStream.of(dya).map(Math::abs).max().orElseGet(() -> 0.0));
        System.out.printf("dy < 10%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.1).count()/(double)allCount*100);
        System.out.printf("dy < 5%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.05).count()/(double)allCount*100);
        System.out.printf("dy < 3%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.03).count()/(double)allCount*100);
        System.out.println("=======================================");


        plotCharts("abs.png", y, res);
        plotCharts("dy.png", dyp);
        plotCharts("d.png", new ArrayRealVector(DoubleStream.of(dy.toArray()).map(Math::abs).sorted().toArray()));
    }

    private static void plotCharts(String fileName, RealMatrix... matrices) throws IOException {
        ImageIO.write(plotSeries(convertToSeries(matrices)), "png", new File(fileName));
    }

    private static void plotCharts(String fileName, RealVector... matrices) throws IOException {
        ImageIO.write(plotSeries(convertToSeries(matrices)), "png", new File(fileName));
    }

    private static BufferedImage plotSeries(java.util.List<XYSeries> s) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        s.stream().forEach(dataset::addSeries);

        BufferedImage image = new BufferedImage(1920, 1080, 1);
        Graphics2D graphics2D = image.createGraphics();

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "Load",
                "date",
                "Load",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);


        xylineChart.setBackgroundPaint(Color.white);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));



        XYPlot plot = (XYPlot) xylineChart.getPlot();
        plot.setRenderer(renderer);

        plot.draw(graphics2D, new Rectangle(0, 0, 1919, 1079), null, null, null);
        return image;
    }

    private static java.util.List<XYSeries> convertToSeries(RealMatrix... matrices) {
        return Stream.of(matrices).map(RealMatrix::getData).map(a -> {
            XYSeries s = new XYSeries(Arrays.hashCode(a));
            for (int i = 0; i < a.length; i++) {
                s.add(i, a[i][0]);
            }
            return s;
        }).collect(Collectors.toList());
    }

    private static java.util.List<XYSeries> convertToSeries(RealVector... matrices) {
        return Stream.of(matrices).map(RealVector::toArray).map(a -> {
            XYSeries s = new XYSeries(Arrays.hashCode(a));
            for (int i = 0; i < a.length; i++) {
                s.add(i, a[i]);
            }
            return s;
        }).collect(Collectors.toList());
    }

    private static String getMatrixRowString(Map<LocalDate, WeatherModel> map, WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return getDataBuilder(map, model, holidays, load).build();
    }

    private static double[] getXMatrixRowArray(Map<LocalDate, WeatherModel> map, WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return getDataBuilder(map, model, holidays, load).buildXArrayRow();
    }

    private static double[] getYMatrixRowArray(WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return new double[]{load.get(model.getDate())};
    }

    public static MatrixBuilder getDataBuilder(Map<LocalDate, WeatherModel> map, WeatherModel model, Holidays holidays, Map<LocalDate, Double> load) {
        return new MatrixBuilder(load.get(model.getDate()))

                //Integral parameters
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6), same)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6), cubic)

                .addParameter(getIntegralParam(map, model, "clouds", 3), same)
                .addParameter(getIntegralParam(map, model, "clouds", 3), signedSquare)
                .addParameter(getIntegralParam(map, model, "clouds", 3), cubic)

                .addParameter(getIntegralParam(map, model, "avgHumidity", 3), same)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3), cubic)

                .addParameter(getIntegralParam(map, model, "precipitation", 3), same)
                .addParameter(getIntegralParam(map, model, "precipitation", 3), signedSquare)
                .addParameter(getIntegralParam(map, model, "precipitation", 3), cubic)

                .addParameter(getIntegralParam(map, model, "wind", 3), same)
                .addParameter(getIntegralParam(map, model, "wind", 3), signedSquare)
                .addParameter(getIntegralParam(map, model, "wind", 3), cubic)


                //Holidays
                .addParameter(holidays.religious(model.getDate()), d -> d * -100)
                .addParameter(holidays.state(model.getDate()), d -> d * -100)
                .addParameter(holidays.school(model.getDate()), d -> d * -100)

                .addParameter(holidays.religious(model.getDate())*holidays.religious(model.getDate().minusDays(1))*holidays.religious(model.getDate().plusDays(1)), d -> d * -100)
                .addParameter(holidays.religious(model.getDate())+holidays.religious(model.getDate().minusDays(1))+holidays.religious(model.getDate().plusDays(1)), d -> d * -100)
                .addParameter(holidays.religious(model.getDate())*holidays.state(model.getDate().minusDays(1))*holidays.state(model.getDate().plusDays(1)), d -> d * -100)


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
                .addParameter(model.getMaxHumidity(), signedSquare)
                .addParameter(model.getMaxTemperature(), signedSquare)
                .addParameter(model.getMinHumidity(), signedSquare)
                .addParameter(model.getMinTemperature(), signedSquare)
                .addParameter(model.getPrecipitation(), signedSquare)
                .addParameter(model.getPressure(), signedSquare)
                .addParameter(model.getVisibility(), signedSquare)
                .addParameter(model.getWind(), signedSquare)
                .addParameter(model.getSunRiseBeforeWork(), signedSquare)
                .addParameter(model.getSunSetBeforeWork(), signedSquare)

                //Cubic weather parameters
                .addParameter(model.getAstronomicalDayLong(), cubic)
                .addParameter(model.getAvgHumidity(), cubic)
                .addParameter(model.getAvgTemperature(), cubic)
                .addParameter(model.getSunRiseBeforeWork(), cubic)
                .addParameter(model.getSunSetBeforeWork(), cubic)

                //SQRT
                .addParameter(model.getClouds(), sqrt)
                .addParameter(model.getDayLightLong(), sqrt)
                .addParameter(model.getMaxHumidity(), sqrt)
                .addParameter(model.getPrecipitation(), sqrt)
                .addParameter(model.getPressure(), sqrt)
                .addParameter(model.getVisibility(), sqrt)
                .addParameter(model.getWind(), sqrt)

                //Special parameters
                .addParameter(isDayLightSaving(model.getDate()), same)

                .addParameter(model.getAvgTemperature() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getAvgTemperature() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getAvgTemperature() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.school(model.getDate()), cubic)

                .addParameter(model.getAstronomicalDayLong() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getAstronomicalDayLong() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getAstronomicalDayLong() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getAstronomicalDayLong() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getAstronomicalDayLong() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getAstronomicalDayLong() * holidays.school(model.getDate()), cubic)

                .addParameter(model.getSunRiseBeforeWork() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getSunRiseBeforeWork() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getSunRiseBeforeWork() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getSunRiseBeforeWork() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getSunRiseBeforeWork() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getSunRiseBeforeWork() * holidays.school(model.getDate()), cubic)

                .addParameter(model.getSunSetBeforeWork() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getSunSetBeforeWork() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getSunSetBeforeWork() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getSunSetBeforeWork() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getSunSetBeforeWork() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getSunSetBeforeWork() * holidays.school(model.getDate()), cubic)

                .addParameter((model.getSunSetBeforeWork() + isDayLightSaving(model.getDate())*3600) * holidays.religious(model.getDate()), cubic)
                .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate())*3600) * holidays.religious(model.getDate()), cubic)
                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600) * holidays.state(model.getDate()), cubic)
                .addParameter((model.getSunRiseBeforeWork() - isDayLightSaving(model.getDate())*3600) * holidays.state(model.getDate()), cubic)
                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600) * holidays.school(model.getDate()), cubic)
                .addParameter((model.getSunRiseBeforeWork() - isDayLightSaving(model.getDate())*3600) * holidays.school(model.getDate()), cubic)
                ;
    }

    private static double getIntegralParam(Map<LocalDate, WeatherModel> map, WeatherModel model, String paramName, int daysCount) {
        return Stream.iterate(model.getDate(), date -> date.minusDays(1))
                .limit(daysCount)
                .map(map::get)
                .filter(m -> m != null)
                .mapToDouble(m -> getValue(m, paramName))
                .average()
                .orElseGet(() -> 0.0)*daysCount;
    }

    private static double getValue(WeatherModel model, String paramName) {
        try {
            Field field = WeatherModel.class.getDeclaredField(paramName);
            field.setAccessible(true);
            return field.getDouble(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static WeatherModel getDaysBefore(Map<LocalDate, WeatherModel> modelMap, WeatherModel model, int i) {
        return Optional.ofNullable(modelMap.get(model.getDate().minusDays(i))).orElseGet(() -> model);
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
