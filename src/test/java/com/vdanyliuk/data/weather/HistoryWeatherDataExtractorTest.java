package com.vdanyliuk.data.weather;

import com.vdanyliuk.data.weather.historical.HistoryWeatherDataExtractor;
import org.junit.Before;

public class HistoryWeatherDataExtractorTest {

    public static final WeatherModel WEATHER_MODEL = WeatherModel.builder()
            .astronomicalDayLong(8*60+19)
            .dayLightLong(9*60+33)
            .avgHumidity(78)
            .maxHumidity(92)
            .minHumidity(60)
            .avgTemperature(-12)
            .maxTemperature(-7)
            .minTemperature(-17)
            .dewPoint(-14)
            .precipitation(0)
            .pressure(1027.39)
            .wind(3)
            .visibility(10.6)
            .build();

    private HistoryWeatherDataExtractor extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new HistoryWeatherDataExtractor(HistoricalWeatherDataProviderTest.DOCUMENT, HistoricalWeatherDataProviderTest.TEST_DATE);
    }

/*    @Test
    public void testGetWeather() throws Exception {
        assertEquals(WEATHER_MODEL, extractor.getWeather());
    }*/
}