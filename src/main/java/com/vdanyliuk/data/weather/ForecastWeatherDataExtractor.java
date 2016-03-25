package com.vdanyliuk.data.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vdanyliuk.data.Cache;
import com.vdanyliuk.data.DataProvider;
import com.vdanyliuk.data.astronomical.AstronomyData;
import com.vdanyliuk.data.astronomical.StoredAstronomicalDataProvider;
import com.vdanyliuk.data.weather.api.APIResponseToWeatherModelConverter;
import com.vdanyliuk.data.weather.api.HourlyWeather;
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
public class ForecastWeatherDataExtractor implements WeatherDataProvider{

    private static final String URL = "https://api.forecast.io/forecast/80670f9deea5e66cd05bc243c5792921/48.9131692,24.7025118";

    private DataProvider<AstronomyData> astronomyDataProvider;
    private APIResponseToWeatherModelConverter modelConverter;
    private ObjectMapper mapper;

    public ForecastWeatherDataExtractor(DataProvider<AstronomyData> astronomyDataProvider) {
        this.astronomyDataProvider = astronomyDataProvider;
        modelConverter = new APIResponseToWeatherModelConverter(astronomyDataProvider);
        mapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        DataProvider<AstronomyData> astronomyDataDataProvider = Cache.load("data/astronomy.dat", StoredAstronomicalDataProvider.class);

        ForecastWeatherDataExtractor dataExtractor = new ForecastWeatherDataExtractor(astronomyDataDataProvider);
        System.out.println(dataExtractor.getData(LocalDate.now().plusDays(1)));
    }


    @Override
    public WeatherModel getData(LocalDate date) {
        return modelConverter.convert(getHourlyWeather(date));
    }

    public List<HourlyWeather> getHourlyWeather(LocalDate date){
        JsonNode response = getURL(URL);

        JsonNode hourlyDataWraper = response.findValue("hourly");

        ArrayNode hourlyData = (ArrayNode) hourlyDataWraper.findValue("data");

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
