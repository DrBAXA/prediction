package com.vdanyliuk;

import com.vdanyliuk.analize.ChartPloter;
import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.Holidays;
import com.vdanyliuk.data.load.LoadDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.historical.CloudsDataProvider;
import com.vdanyliuk.data.weather.historical.HistoricalWeatherDataProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Test1 {

    private static DataProvider<Double> cloudsDataProvider = new CloudsDataProvider();
    private static DataProvider<WeatherModel> historicalWeatherDataProvider = Cache.load("data/historycalWeatherData.cache", HistoricalWeatherDataProvider.class, cloudsDataProvider);


    public static void main(String[] args) throws IOException {
        LoadDataProvider loadDataProvider = new LoadDataProvider("data/load.csv");

        List<Double> temp = new ArrayList<>();
        List<Double> load = new ArrayList<>();

        Stream
                .iterate(LocalDate.of(2015, 3, 1), d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(LocalDate.of(2015, 3, 1), LocalDate.now()))
                .filter(loadDataProvider::contains)
                .filter(d -> Holidays.religious(d) == 0)
                .filter(d -> Holidays.state(d) == 0)
                .forEach(date -> {
                    temp.add(historicalWeatherDataProvider.getData(date).getWind());
                    load.add(loadDataProvider.getData(date));
                });

        double[] tempArray = temp.stream().mapToDouble(d -> d).toArray();
        double[] loadArray = load.stream().mapToDouble(d -> d).toArray();

        ChartPloter.plotdependChart("LoadFromWind.png", tempArray, loadArray);

    }

}
