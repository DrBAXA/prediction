package com.vdanyliuk;

import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.DateBasedWeatherDataModel;
import com.vdanyliuk.data.astronomical.AstronomyData;
import com.vdanyliuk.data.astronomical.StoredAstronomicalDataProvider;
import com.vdanyliuk.data.weather.ForecastWeatherDataExtractor;
import com.vdanyliuk.data.weather.VisibilityDataProvider;
import com.vdanyliuk.data.weather.WUndergroundWeatherDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.solver.EnergyLoadWeatherSolver;
import com.vdanyliuk.solver.RegressionSolver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

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

        WUndergroundWeatherDataProvider dataProvider = new WUndergroundWeatherDataProvider();
        DataProvider<AstronomyData> astronomyDataProvider = Cache.load("data/astronomy.dat", StoredAstronomicalDataProvider.class);
        DataProvider<Double>  visibilityDataProvider = new VisibilityDataProvider();
        ForecastWeatherDataExtractor forecastWeatherDataExtractor = new ForecastWeatherDataExtractor(astronomyDataProvider, visibilityDataProvider);

        DateBasedWeatherDataModel dataModel = new DateBasedWeatherDataModel(dataProvider,
                LocalDate.of(2015, 3, 1),
                LocalDate.of(2016, 4, 2));

        RegressionSolver<WeatherModel> solver = new EnergyLoadWeatherSolver(dataModel, dataModel);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        WeatherModel tomorrowData = forecastWeatherDataExtractor.getData(tomorrow);

        System.out.println(solver.solve(tomorrowData));

    }

}
