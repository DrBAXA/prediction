package com.vdanyliuk.data.weather.forecast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.weather.forecast.astronomical.AstronomyData;
import com.vdanyliuk.data.weather.WeatherDataProvider;
import com.vdanyliuk.data.weather.WeatherModel;
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
public class ForecastWeatherDataProvider implements WeatherDataProvider {

    private static final String URL = "https://api.forecast.io/forecast/80670f9deea5e66cd05bc243c5792921/48.9131692,24.7025118";

    private ResponseToWeatherModelConverter modelConverter;
    private ObjectMapper mapper;

    public ForecastWeatherDataProvider(DataProvider<AstronomyData> astronomyDataProvider, DataProvider<Double> visibilityDataProvider) {
        modelConverter = new ResponseToWeatherModelConverter(astronomyDataProvider, visibilityDataProvider);
        mapper = new ObjectMapper();
    }

    @Override
    public WeatherModel getData(LocalDate date) {
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
