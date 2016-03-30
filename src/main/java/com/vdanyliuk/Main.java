package com.vdanyliuk;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.forecast.ForecastWeatherDataProvider;
import com.vdanyliuk.data.weather.forecast.VisibilityDataProvider;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomyData;
import com.vdanyliuk.data.weather.forecast.astronomical.StoredAstronomicalDataProvider;
import com.vdanyliuk.data.weather.historical.HistoricalWeatherDataProvider;
import com.vdanyliuk.solver.EnergyLoadWeatherSolver;
import com.vdanyliuk.solver.RegressionSolver;

import java.io.IOException;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) throws IOException {
        prepareFiles();
    }

    public static void prepareFiles() throws IOException {

        HistoricalWeatherDataProvider dataProvider = Cache.load("data/historycalWeatherData.cache", HistoricalWeatherDataProvider.class);
        DataProvider<AstronomyData> astronomyDataProvider = Cache.load("data/astronomy.dat", StoredAstronomicalDataProvider.class);
        DataProvider<Double>  visibilityDataProvider = new VisibilityDataProvider();
        ForecastWeatherDataProvider forecastWeatherDataProvider = new ForecastWeatherDataProvider(astronomyDataProvider, visibilityDataProvider);

        DateBasedWeatherDataModel dataModel = new DateBasedWeatherDataModel(dataProvider,
                LocalDate.of(2015, 3, 1),
                LocalDate.of(2016, 4, 2));

        dataProvider.store("data/historycalWeatherData.cache");

        RegressionSolver<WeatherModel> solver = new EnergyLoadWeatherSolver(dataModel, dataModel);

        LocalDate tomorrow = LocalDate.now().plusDays(0);

        WeatherModel tomorrowData = forecastWeatherDataProvider.getData(tomorrow);

        System.out.println(solver.solve(tomorrowData));
    }

}
