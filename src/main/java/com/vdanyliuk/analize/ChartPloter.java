package com.vdanyliuk.analize;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
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
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChartPloter {
    public static void plotCharts(String fileName, RealMatrix... matrices) throws IOException {
        ImageIO.write(plotSeries(convertToSeries(matrices)), "png", new File(fileName));
    }

    public static void plotCharts(String fileName, RealVector... matrices) throws IOException {
        ImageIO.write(plotSeries(convertToSeries(matrices)), "png", new File(fileName));
    }

    public static void plotdependChart(String fileName, double[] x, double y[]) throws IOException {
        ImageIO.write(plotSeries(convertToSeries(x, y, 5)), "png", new File(fileName));
    }

    private static BufferedImage plotSeries(XYSeries s) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s);

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

    private static XYSeries convertToSeries(double[] x, double y[], int name) {
        if (x.length != y.length) throw new IllegalArgumentException("arrays should bhave same size.");
        XYSeries s = new XYSeries(name);
        for (int i = 0; i < x.length; i++) {
            s.add(x[i], y[i]);
        }
        return s;
    }
}

