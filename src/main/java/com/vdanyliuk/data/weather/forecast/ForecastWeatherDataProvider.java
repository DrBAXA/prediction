package com.vdanyliuk.data.weather.forecast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomyData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class ForecastWeatherDataProvider extends Cache<LocalDate, WeatherModel> {

    private static final long serialVersionUID = 1L;

    private static final String URL = "https://api.forecast.io/forecast/80670f9deea5e66cd05bc243c5792921/48.9131692,24.7025118";

    private transient ResponseToWeatherModelConverter modelConverter;
    private static final ObjectMapper mapper  =new ObjectMapper();;
    private DataProvider<AstronomyData> astronomyDataProvider;
    private DataProvider<Double> visibilityDataProvider;

    public ForecastWeatherDataProvider(DataProvider<AstronomyData> astronomyDataProvider, DataProvider<Double> visibilityDataProvider) {
        this.astronomyDataProvider = astronomyDataProvider;
        this.visibilityDataProvider = visibilityDataProvider;
        modelConverter = new ResponseToWeatherModelConverter(astronomyDataProvider, visibilityDataProvider);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        modelConverter = new ResponseToWeatherModelConverter(astronomyDataProvider, visibilityDataProvider);
    }

    @Override
    protected WeatherModel getNonCachedData(LocalDate date) {
        return modelConverter.convert(getHourlyWeather(date));
    }

    public List<HourlyWeather> getHourlyWeather(LocalDate date){
        JsonNode response = getURL(URL);

        JsonNode hourlyDataWrapper = response.findValue("hourly");

        ArrayNode hourlyData = (ArrayNode) hourlyDataWrapper.findValue("data");

        return StreamSupport
                .stream(hourlyData.spliterator(), false)
                .map(getNodeToPOJOMapper(mapper, HourlyWeather.class))
                .filter(hw -> LocalDateTime.ofEpochSecond(hw.getTimestamp(), 0, ZoneOffset.ofHours(2)).toLocalDate().equals(date))
                .collect(Collectors.toList());

    }

    private JsonNode getURL(String url) {
        try {
            return mapper.readTree(new URL(URL));
        } catch (IOException e) {
            log.error("Can't get page " + url);
            return null;
        }
    }

    private <T> Function<JsonNode, T> getNodeToPOJOMapper(ObjectMapper objectMapper, Class<T> pojoClass) {
        return node -> {
            try {
                return objectMapper.treeToValue(node, pojoClass);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
    

}
