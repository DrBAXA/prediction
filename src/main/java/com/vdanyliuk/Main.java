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
                .map(wr -> getYMatrixRowArray(wr, load))
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
        System.out.printf("max dy = %.3f%n", DoubleStream.of(dya).map(Math::abs).max().orElseGet(() -> 0.0));
        System.out.printf("dy < 2%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.02).count()/(double)allCount*100);
        System.out.printf("dy < 1%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.01).count()/(double)allCount*100);
        System.out.printf("dy < 0.5%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.005).count()/(double)allCount*100);
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

    private static double[] getYMatrixRowArray(WeatherModel model, Map<LocalDate, Double> load) {
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

                .addParameter(getIntegralParam(map, model, "pressure", 3), same)

                //Holidays
                .addParameter(holidays.religious(model.getDate()), d -> d * -100)
                .addParameter(holidays.state(model.getDate()), d -> d * -100)
                .addParameter(holidays.school(model.getDate()), d -> d * -100)

                .addParameter(holidays.religious(model.getDate())*holidays.religious(model.getDate().minusDays(2))*holidays.religious(model.getDate().minusDays(1)), d -> d * -100)
                .addParameter(holidays.religious(model.getDate())+holidays.religious(model.getDate().minusDays(2))+holidays.religious(model.getDate().minusDays(1)), d -> d * -100)
                .addParameter(holidays.religious(model.getDate())*holidays.state(model.getDate().minusDays(2))*holidays.state(model.getDate().minusDays(1)), d -> d * -100)

                .addParameter(holidays.state(model.getDate().minusDays(1)), d -> d * -100)
                //.addParameter(holidays.state(model.getDate().plusDays(1)), d -> d * -100)


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

                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgTemperature", 6) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getAvgTemperature() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getAvgTemperature() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getAvgTemperature() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getAvgTemperature() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "clouds", 3) * holidays.school(model.getDate()), signedSquare)


                .addParameter(model.getClouds() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getClouds() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getClouds() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getClouds() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getClouds() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getClouds() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "wind", 4) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getWind() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getWind() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getWind() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getWind() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getWind() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getWind() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "precipitation", 4) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getPrecipitation() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getPrecipitation() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getPrecipitation() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getPrecipitation() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getPrecipitation() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getPrecipitation() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "avgHumidity", 3) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getAvgHumidity() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getAvgHumidity() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getAvgHumidity() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getAvgHumidity() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getAvgHumidity() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getAvgHumidity() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "visibility", 3) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getVisibility() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getVisibility() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getVisibility() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getVisibility() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getVisibility() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getVisibility() * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.state(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.state(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.religious(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.religious(model.getDate()), cubic)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.school(model.getDate()), d -> -d)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.school(model.getDate()), cubic)

                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.state(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.religious(model.getDate()), signedSquare)
                .addParameter(getIntegralParam(map, model, "dewPoint", 4) * holidays.school(model.getDate()), signedSquare)

                .addParameter(model.getDewPoint() * holidays.state(model.getDate()), d -> -d)
                .addParameter(model.getDewPoint() * holidays.state(model.getDate()), cubic)
                .addParameter(model.getDewPoint() * holidays.religious(model.getDate()), d -> -d)
                .addParameter(model.getDewPoint() * holidays.religious(model.getDate()), cubic)
                .addParameter(model.getDewPoint() * holidays.school(model.getDate()), d -> -d)
                .addParameter(model.getDewPoint() * holidays.school(model.getDate()), cubic)

                .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), same)
                .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), signedSquare)
                .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), cubic)

                .addParameter(model.getAstronomicalDayLong() * model.getClouds(), same)
                .addParameter(model.getAstronomicalDayLong() * model.getClouds(), signedSquare)
                .addParameter(model.getAstronomicalDayLong() * model.getClouds(), cubic)

                .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), same)
                .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), signedSquare)
                .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), cubic)

                .addParameter(model.getDate().isBefore(LocalDate.of(2015, 9, 1)) && model.getDate().isAfter(LocalDate.of(2015, 5, 31)) ? 20.0 : 0.0, same)

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

                .addParameter((model.getSunSetBeforeWork()*model.getClouds()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getClouds()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getClouds()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getClouds()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getClouds()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getClouds()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "clouds", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "clouds", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "clouds", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "clouds", 3)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "clouds", 3)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "clouds", 3)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getAvgTemperature()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getAvgTemperature()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getAvgTemperature()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getAvgTemperature()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getAvgTemperature()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getAvgTemperature()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "avgTemperature", 6)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getWind()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getWind()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getWind()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getWind()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getWind()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getWind()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "wind", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "wind", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "wind", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "wind", 3)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "wind", 3)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "wind", 3)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getPrecipitation()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getPrecipitation()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getPrecipitation()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getPrecipitation()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getPrecipitation()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getPrecipitation()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "precipitation", 3)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getPressure()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getPressure()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getPressure()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getPressure()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getPressure()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getPressure()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "pressure", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "pressure", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "pressure", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "pressure", 3)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "pressure", 3)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "pressure", 3)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getDewPoint()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getDewPoint()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getDewPoint()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getDewPoint()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getDewPoint()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getDewPoint()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), cubic)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "dewPoint", 3)), cubic)

                .addParameter((model.getSunSetBeforeWork()*model.getVisibility()), same)
                .addParameter((model.getSunRiseBeforeWork()*model.getVisibility()), same)
                .addParameter((model.getSunSetBeforeWork()*model.getVisibility()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*model.getVisibility()), signedSquare)
                .addParameter((model.getSunSetBeforeWork()*model.getVisibility()), cubic)
                .addParameter((model.getSunRiseBeforeWork()*model.getVisibility()), cubic)

                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "visibility", 3)), same)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "visibility", 3)), same)
                .addParameter((model.getSunSetBeforeWork()*getIntegralParam(map, model, "visibility", 3)), signedSquare)
                .addParameter((model.getSunRiseBeforeWork()*getIntegralParam(map, model, "visibility", 3)), signedSquare)


                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600), signedSquare)
                .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate())*3600), signedSquare)
                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600), cubic)
                .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate())*3600), cubic)

                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600*model.getClouds()), signedSquare)
                .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate())*3600*model.getClouds()), signedSquare)
                .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate())*3600*model.getClouds()), cubic)
                .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate())*3600*model.getClouds()), cubic)
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
