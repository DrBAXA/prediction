package com.vdanyliuk;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.data.load.LoadDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.historical.CloudsDataProvider;
import com.vdanyliuk.data.weather.historical.HistoricalWeatherDataProvider;
import com.vdanyliuk.solver.EnergyLoadWeatherSolver;
import com.vdanyliuk.solver.RegressionSolver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class Test {

    private static DataProvider<Double> cloudsDataProvider = new CloudsDataProvider();
    private static DataProvider<WeatherModel> historicalWeatherDataProvider = Cache.load("data/historycalWeatherData.cache", HistoricalWeatherDataProvider.class, cloudsDataProvider);


    public static void main(String[] args) throws IOException {
        LoadDataProvider loadDataProvider = new LoadDataProvider("data/load.csv");

        Stream
                .iterate(LocalDate.of(2015, 3, 1), d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(LocalDate.of(2015, 3, 1), LocalDate.now()))
                .filter(loadDataProvider::contains)
                .map(d -> testPrediction(loadDataProvider.cloneExcept(d), d) - loadDataProvider.getData(d))
                .map(Math::abs)
                .sorted()
                .forEach(System.out::println);


    }

    private static Double testPrediction(LoadDataProvider loadDataProvider, LocalDate date) {
        DateBasedWeatherDataModel dataModel = null;
        try {
            dataModel = new DateBasedWeatherDataModel(historicalWeatherDataProvider, loadDataProvider,
                    LocalDate.of(2015, 3, 1),
                    LocalDate.now());
        } catch (IOException e) {
            e.printStackTrace();
        }

        RegressionSolver<WeatherModel> solver = new EnergyLoadWeatherSolver(dataModel, dataModel, false);

        WeatherModel predictionData = historicalWeatherDataProvider.getData(date);

        return solver.solve(predictionData);
    }
}
