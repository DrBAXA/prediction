package com.vdanyliuk;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.data.load.LoadDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.forecast.ForecastWeatherDataProvider;
import com.vdanyliuk.data.weather.forecast.VisibilityDataProvider;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomyData;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomicalDataProvider;
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

    private Cache<LocalDate, Double> cloudsDataProvider;
    private Cache<LocalDate, AstronomyData> astronomyDataProvider;

    private Cache<LocalDate, Double> visibilityDataProvider;
    private HistoricalWeatherDataProvider historicalWeatherDataProvider;
    private ForecastWeatherDataProvider forecastWeatherDataProvider;

    private DataProvider<Double> loadDataProvider;

    public static void main(String[] args) throws IOException, ParseException {
        Main app = new Main(args);
        app.predict();
    }

    public Main(String... args) throws IOException {
        parseCommandLine(args);
        cloudsDataProvider = new CloudsDataProvider();
        historicalWeatherDataProvider = Cache.load("data/historycalWeatherData.cache", HistoricalWeatherDataProvider.class, cloudsDataProvider);

        astronomyDataProvider = new AstronomicalDataProvider();
        visibilityDataProvider = new VisibilityDataProvider();
        forecastWeatherDataProvider = Cache.load("data/forecastData.cache", ForecastWeatherDataProvider.class, astronomyDataProvider, visibilityDataProvider);

        loadDataProvider = new LoadDataProvider("data/load.csv");
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
        DateBasedWeatherDataModel dataModel = new DateBasedWeatherDataModel(historicalWeatherDataProvider, loadDataProvider,
                LocalDate.of(2015, 3, 1),
                LocalDate.now());
        historicalWeatherDataProvider.store("data/historycalWeatherData.cache");

        RegressionSolver<WeatherModel> solver = new EnergyLoadWeatherSolver(dataModel, dataModel, getDebugMode());

        WeatherModel predictionData = getPredictionData(getDate());

        System.out.println("Predicted energy consumption on " + getDate() + " " + solver.solve(predictionData));
    }

    private WeatherModel getPredictionData(LocalDate date) {
        WeatherModel model;
        if (date.isBefore(LocalDate.now()) || date.equals(LocalDate.now())) {
            model = historicalWeatherDataProvider.getData(date);
        } else {
            model = forecastWeatherDataProvider.getData(date);
        }

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
