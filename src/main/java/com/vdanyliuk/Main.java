package com.vdanyliuk;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.forecast.ForecastWeatherDataProvider;
import com.vdanyliuk.data.weather.forecast.VisibilityDataProvider;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomyData;
import com.vdanyliuk.data.weather.forecast.astronomical.StoredAstronomicalDataProvider;
import com.vdanyliuk.data.weather.historical.CloudsDataProvider;
import com.vdanyliuk.data.weather.historical.HistoricalWeatherDataProvider;
import com.vdanyliuk.solver.EnergyLoadWeatherSolver;
import com.vdanyliuk.solver.RegressionSolver;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main {

    private CommandLine commandLine;

    Cache<LocalDate, Double> cloudsDataProvider;
    Cache<LocalDate, AstronomyData> astronomyDataProvider;
    Cache<LocalDate, Double> visibilityDataProvider;
    HistoricalWeatherDataProvider historicalWeatherDataProvider;
    ForecastWeatherDataProvider forecastWeatherDataProvider;

    public static void main(String[] args) throws IOException, ParseException {
        Main app = new Main(args);
        app.predict();
    }

    public Main(String... args) {
        parseCommandLine(args);
        cloudsDataProvider = Cache.load("data/clouds.cache", CloudsDataProvider.class);
        historicalWeatherDataProvider = Cache.load("data/historycalWeatherData.cache", HistoricalWeatherDataProvider.class, cloudsDataProvider);
        astronomyDataProvider = Cache.load("data/astronomy.dat", StoredAstronomicalDataProvider.class);
        visibilityDataProvider = Cache.load("data/visibility.cache", VisibilityDataProvider.class);
        forecastWeatherDataProvider = Cache.load("data/forecastData.cache", ForecastWeatherDataProvider.class, astronomyDataProvider, visibilityDataProvider);
    }

    private boolean getDebugMode() {
        return commandLine.hasOption("debug");
    }

    private LocalDate getDate() {
        String dateString = commandLine.getOptionValue("date");
        try {
            return dateString != null ? LocalDate.parse(dateString) : LocalDate.now().plusDays(1);
        } catch (DateTimeParseException e) {
            System.out.println("Wrong date format");
            System.exit(1);
            return null;
        }

    }

    private void parseCommandLine(String... args) {
        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(getOptions(), args, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void predict() throws IOException {
        DateBasedWeatherDataModel dataModel = new DateBasedWeatherDataModel(historicalWeatherDataProvider,
                LocalDate.of(2015, 3, 1),
                LocalDate.now());
        cloudsDataProvider.store("data/clouds.cache");
        historicalWeatherDataProvider.store("data/historycalWeatherData.cache");

        RegressionSolver<WeatherModel> solver = new EnergyLoadWeatherSolver(dataModel, dataModel, getDebugMode());

        WeatherModel predictionData = getPredictionData(getDate());



        System.out.println(solver.solve(predictionData));
    }

    private WeatherModel getPredictionData(LocalDate date) {
        WeatherModel model;
        if (date.isBefore(LocalDate.now()) || date.equals(LocalDate.now())) {
            model = historicalWeatherDataProvider.getData(date);
        } else {
            model = forecastWeatherDataProvider.getData(date);
        }

        astronomyDataProvider.store("data/astronomy.dat");
        visibilityDataProvider.store("data/visibility.cache");
        forecastWeatherDataProvider.store("data/forecastData.cache");

        return model;
    }


    private static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder("d")
                .longOpt("debug")
                .desc("Enable additional information printing.")
                .hasArg(false)
                .required(false)
                .build());

        options.addOption(Option.builder("dt")
                .longOpt("date")
                .desc("Date for predicting")
                .hasArg(true)
                .numberOfArgs(1)
                .type(LocalDate.class)
                .valueSeparator(' ')
                .required(false)
                .build());

        return options;
    }

}
