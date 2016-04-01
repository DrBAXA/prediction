package com.vdanyliuk.data;

import com.vdanyliuk.data.weather.WeatherModel;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


@Slf4j
public class DateBasedWeatherDataModel implements IndependentDataModel<WeatherModel>, DependentDataModel, Serializable {

    private static final UnaryOperator<Double> same = d -> d;
//  private static final UnaryOperator<Double> signedSquare = d -> Math.abs(d) * d;
//  private static final UnaryOperator<Double> cubic = d -> Math.pow(d, 3);
    private static final UnaryOperator<Double> sqrt = Math::sqrt;

    private transient DataProvider<WeatherModel> weatherDataProvider;
    private transient DataProvider<Double> loadDataProvider;

    public Map<LocalDate, WeatherModel> weatherData;
    private Map<LocalDate, Double> loadData;

    public DateBasedWeatherDataModel(DataProvider<WeatherModel> weatherDataProvider, DataProvider<Double> loadDataProvider, LocalDate startDate, LocalDate endDate) throws IOException {
        this.weatherDataProvider = weatherDataProvider;
        this.loadDataProvider = loadDataProvider;
        loadData = getLoad(startDate, endDate);
        weatherData = getWeather(startDate, endDate);
    }

    public static DateBasedWeatherDataModel load(File storedData) throws IOException, ClassNotFoundException {
        try (InputStream stream = new FileInputStream(storedData);
             ObjectInputStream inputStream = new ObjectInputStream(stream)) {

            return (DateBasedWeatherDataModel) inputStream.readObject();
        }
    }

    @Override
    public double[][] getXArray() {
        return weatherData.keySet().stream().sorted()
                .map(weatherData::get)
                .map(this::getXMatrixRowArray)
                .toArray(double[][]::new);
    }

    @Override
    public double[][] getYArray() {
        return loadData.keySet().stream().sorted()
                .map(loadData::get)
                .map(v -> new double[]{v})
                .toArray(double[][]::new);
    }

    @Override
    public int size() {
        return loadData.size();
    }

    public double[] getXRow(WeatherModel weatherModel) {
        return getXMatrixRowArray(weatherModel);
    }

    private double[] getXMatrixRowArray(WeatherModel model) {
        return getDataBuilder(model).buildXArrayRow();
    }

    public MatrixBuilder getDataBuilder(WeatherModel model) {
        return new MatrixBuilder()

                //Integral parameters
                .addParameter(getIntegralParam(model, "avgTemperature", 6), same)
//              .addParameter(getIntegralParam(model, "avgTemperature", 6), signedSquare)
//              .addParameter(getIntegralParam(model, "avgTemperature", 6), cubic)

//                .addParameter(getIntegralParam(model, "clouds", 3), same)
//              .addParameter(getIntegralParam(model, "clouds", 3), signedSquare)
//              .addParameter(getIntegralParam(model, "clouds", 3), cubic)

//                .addParameter(getIntegralParam(model, "avgHumidity", 3), same)
//              .addParameter(getIntegralParam(model, "avgHumidity", 3), signedSquare)
//              .addParameter(getIntegralParam(model, "avgHumidity", 3), cubic)

//                .addParameter(getIntegralParam(model, "precipitation", 3), same)
//              .addParameter(getIntegralParam(model, "precipitation", 3), signedSquare)
//              .addParameter(getIntegralParam(model, "precipitation", 3), cubic)

//                .addParameter(getIntegralParam(model, "wind", 3), same)
//              .addParameter(getIntegralParam(model, "wind", 3), signedSquare)
//              .addParameter(getIntegralParam(model, "wind", 3), cubic)

//                .addParameter(getIntegralParam(model, "pressure", 3), same)

                //Holidays
                .addParameter(Holidays.religious(model.getDate()), same)
                .addParameter(Holidays.state(model.getDate()), same)
                .addParameter(Holidays.school(model.getDate()), same)

                .addParameter(Holidays.religious(model.getDate()) + Holidays.religious(model.getDate().minusDays(2)) + Holidays.religious(model.getDate().minusDays(1)), same)
                .addParameter(Holidays.religious(model.getDate()) + Holidays.state(model.getDate().minusDays(2)) + Holidays.state(model.getDate().minusDays(1)), same)

                .addParameter(Holidays.state(model.getDate().minusDays(1)), same)

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
//              .addParameter(model.getAstronomicalDayLong(), signedSquare)
//              .addParameter(model.getAvgHumidity(), signedSquare)
//              .addParameter(model.getAvgTemperature(), signedSquare)
//              .addParameter(model.getClouds(), signedSquare)
//              .addParameter(model.getDayLightLong(), signedSquare)
//              .addParameter(model.getDewPoint(), signedSquare)
//              .addParameter(model.getMaxHumidity(), signedSquare)
//              .addParameter(model.getMaxTemperature(), signedSquare)
//              .addParameter(model.getMinHumidity(), signedSquare)
//              .addParameter(model.getMinTemperature(), signedSquare)
//              .addParameter(model.getPrecipitation(), signedSquare)
//              .addParameter(model.getPressure(), signedSquare)
//              .addParameter(model.getVisibility(), signedSquare)
//              .addParameter(model.getWind(), signedSquare)
//              .addParameter(model.getSunRiseBeforeWork(), signedSquare)
//              .addParameter(model.getSunSetBeforeWork(), signedSquare)
//              //Cubic weather parameters
//              .addParameter(model.getAstronomicalDayLong(), cubic)
//              .addParameter(model.getAvgHumidity(), cubic)
//              .addParameter(model.getAvgTemperature(), cubic)
//              .addParameter(model.getSunRiseBeforeWork(), cubic)
//              .addParameter(model.getSunSetBeforeWork(), cubic)

                //SQRT
                .addParameter(model.getClouds(), sqrt)
                .addParameter(model.getDayLightLong(), sqrt)
                .addParameter(model.getAvgTemperature(), sqrt)
                .addParameter(model.getPressure(), sqrt)
                .addParameter(model.getVisibility(), sqrt)
                .addParameter(model.getWind(), sqrt)

                //Special parameters
                .addParameter(isDayLightSaving(model.getDate()), same)
//                .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "avgTemperature", 6) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getAvgTemperature() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getAvgTemperature() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getAvgTemperature() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getAvgTemperature() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getAvgTemperature() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getAvgTemperature() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "clouds", 3) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getClouds() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getClouds() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getClouds() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getClouds() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getClouds() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getClouds() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "wind", 4) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "wind", 4) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "wind", 4) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "wind", 4) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getWind() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getWind() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getWind() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getWind() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getWind() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getWind() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "precipitation", 4) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getPrecipitation() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getPrecipitation() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getPrecipitation() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getPrecipitation() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getPrecipitation() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getPrecipitation() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "avgHumidity", 3) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getAvgHumidity() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getAvgHumidity() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getAvgHumidity() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getAvgHumidity() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getAvgHumidity() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getAvgHumidity() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "visibility", 3) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getVisibility() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getVisibility() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getVisibility() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getVisibility() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getVisibility() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getVisibility() * Holidays.school(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.state(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.state(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.religious(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.religious(model.getDate()), cubic)
//                .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.school(model.getDate()), same)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.school(model.getDate()), cubic)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.state(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.religious(model.getDate()), signedSquare)
////              .addParameter(getIntegralParam(model, "dewPoint", 4) * Holidays.school(model.getDate()), signedSquare)
//                .addParameter(model.getDewPoint() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getDewPoint() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getDewPoint() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getDewPoint() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getDewPoint() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getDewPoint() * Holidays.school(model.getDate()), cubic)
//                .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), same)
////              .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), signedSquare)
////              .addParameter(model.getAstronomicalDayLong() * model.getAvgTemperature(), cubic)
//                .addParameter(model.getAstronomicalDayLong() * model.getClouds(), same)
////              .addParameter(model.getAstronomicalDayLong() * model.getClouds(), signedSquare)
////              .addParameter(model.getAstronomicalDayLong() * model.getClouds(), cubic)
//                .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), same)
////              .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), signedSquare)
////              .addParameter(model.getAstronomicalDayLong() * model.getPrecipitation(), cubic)
//                .addParameter(model.getDate().isBefore(LocalDate.of(2015, 9, 1)) && model.getDate().isAfter(LocalDate.of(2015, 5, 31)) ? 20.0 : 0.0, same)
//                .addParameter(model.getAstronomicalDayLong() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getAstronomicalDayLong() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getAstronomicalDayLong() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getAstronomicalDayLong() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getAstronomicalDayLong() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getAstronomicalDayLong() * Holidays.school(model.getDate()), cubic)
//                .addParameter(model.getSunRiseBeforeWork() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getSunRiseBeforeWork() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getSunRiseBeforeWork() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getSunRiseBeforeWork() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getSunRiseBeforeWork() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getSunRiseBeforeWork() * Holidays.school(model.getDate()), cubic)
//                .addParameter(model.getSunSetBeforeWork() * Holidays.state(model.getDate()), same)
////              .addParameter(model.getSunSetBeforeWork() * Holidays.state(model.getDate()), cubic)
//                .addParameter(model.getSunSetBeforeWork() * Holidays.religious(model.getDate()), same)
////              .addParameter(model.getSunSetBeforeWork() * Holidays.religious(model.getDate()), cubic)
//                .addParameter(model.getSunSetBeforeWork() * Holidays.school(model.getDate()), same)
////              .addParameter(model.getSunSetBeforeWork() * Holidays.school(model.getDate()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getClouds()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getClouds()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getClouds()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getClouds()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getClouds()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getClouds()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "clouds", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "clouds", 3)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "clouds", 3)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "clouds", 3)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "clouds", 3)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "clouds", 3)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getAvgTemperature()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getAvgTemperature()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getAvgTemperature()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getAvgTemperature()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getAvgTemperature()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getAvgTemperature()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "avgTemperature", 6)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getWind()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getWind()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getWind()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getWind()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getWind()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getWind()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "wind", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "wind", 3)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "wind", 3)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "wind", 3)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "wind", 3)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "wind", 3)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getPrecipitation()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getPrecipitation()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getPrecipitation()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getPrecipitation()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getPrecipitation()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getPrecipitation()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "precipitation", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "precipitation", 3)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "precipitation", 3)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "precipitation", 3)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "precipitation", 3)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "precipitation", 3)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getPressure()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getPressure()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getPressure()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getPressure()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getPressure()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getPressure()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "pressure", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "pressure", 3)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "pressure", 3)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "pressure", 3)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "pressure", 3)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "pressure", 3)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getDewPoint()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getDewPoint()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getDewPoint()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getDewPoint()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getDewPoint()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getDewPoint()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "dewPoint", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "dewPoint", 3)), same)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "dewPoint", 3)), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "dewPoint", 3)), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "dewPoint", 3)), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "dewPoint", 3)), cubic)
//                .addParameter((model.getSunSetBeforeWork() * model.getVisibility()), same)
//                .addParameter((model.getSunRiseBeforeWork() * model.getVisibility()), same)
////              .addParameter((model.getSunSetBeforeWork() * model.getVisibility()), signedSquare)
////              .addParameter((model.getSunRiseBeforeWork() * model.getVisibility()), signedSquare)
////              .addParameter((model.getSunSetBeforeWork() * model.getVisibility()), cubic)
////              .addParameter((model.getSunRiseBeforeWork() * model.getVisibility()), cubic)
//                .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "visibility", 3)), same)
//                .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "visibility", 3)), same)
//              .addParameter((model.getSunSetBeforeWork() * getIntegralParam(model, "visibility", 3)), signedSquare)
//              .addParameter((model.getSunRiseBeforeWork() * getIntegralParam(model, "visibility", 3)), signedSquare)
//              .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate()) * 60), signedSquare)
//              .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate()) * 60), signedSquare)
//              .addParameter((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate()) * 3600), cubic)
//              .addParameter((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate()) * 3600), cubic)
//              .addParameter(((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate()) * 60) * model.getClouds()), signedSquare)
//              .addParameter(((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate()) * 60) * model.getClouds()), signedSquare)
//              .addParameter(((model.getSunSetBeforeWork() - isDayLightSaving(model.getDate()) * 3600) * model.getClouds()), cubic)
//              .addParameter(((model.getSunRiseBeforeWork() + isDayLightSaving(model.getDate()) * 3600) * model.getClouds()), cubic)
                ;
    }

    private double getIntegralParam(WeatherModel model, String paramName, int daysCount) {
        return Stream.iterate(model.getDate(), date -> date.minusDays(1))
                .limit(daysCount)
                .map(weatherData::get)
                .filter(m -> m != null)
                .mapToDouble(m -> getValue(m, paramName))
                .average()
                .orElseGet(() -> 0.0) * daysCount;
    }

    private double getValue(WeatherModel model, String paramName) {
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

    private Map<LocalDate, Double> getLoad(LocalDate startDate, LocalDate endDate) throws IOException {
        return Stream
                .iterate(startDate, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate))
                .filter(d -> loadDataProvider.getData(d) != null)
                .collect(HashMap::new, (m, d) -> m.put(d, loadDataProvider.getData(d)), Map::putAll);
    }

    private Map<LocalDate, WeatherModel> getWeather(LocalDate startDate, LocalDate endDate) {
        return Stream
                .iterate(startDate, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate))
                .filter(loadData::containsKey)
                .map(d -> weatherDataProvider.getData(d))
                .collect(HashMap::new, (m, wm) -> m.put(wm.getDate(), wm), Map::putAll);
    }
}
