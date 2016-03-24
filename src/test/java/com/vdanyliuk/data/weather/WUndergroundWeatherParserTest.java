package com.vdanyliuk.data.weather;

import com.vdanyliuk.util.PropertiesUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertiesUtil.class, Jsoup.class})
public class WUndergroundWeatherParserTest {

    public static final Properties TEST_PROPERTIES = new Properties(){{
        setProperty("connection.url","http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html");
        setProperty("connection.maxTryCount","5");
    }};
    public static final LocalDate TEST_DATE = LocalDate.of(2016, 1, 15);
    public static final String URL_PATTERN_STRING = "http://ukrainian.wunderground.com/history/airport/UKLI/${Date}/DailyHistory.html";
    public static final String REQUIRED_URL = "http://ukrainian.wunderground.com/history/airport/UKLI/2016/01/15/DailyHistory.html";

    public static Connection REQUIRED_CONNECTION;
    public static Document DOCUMENT;

    /*static {
        try {
            DOCUMENT = Jsoup.parse(WUndergroundWeatherParserTest.class.getClassLoader().getResourceAsStream("test.html"), "utf8", "test.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WUndergroundWeatherParser parser = new WUndergroundWeatherParser();

    @Before
    public void init() throws Exception {
        REQUIRED_CONNECTION = mock(Connection.class);

        mockStatic(Jsoup.class);
        mockStatic(PropertiesUtil.class);
        PowerMockito.doReturn(TEST_PROPERTIES).when(PropertiesUtil.class, "loadProperties", anyString());
        PowerMockito.doReturn(REQUIRED_CONNECTION).when(Jsoup.class, "connect", REQUIRED_URL);
    }

    @org.junit.Test
    public void testConstructor() throws Exception {
        assertEquals(TEST_PROPERTIES, parser.getProperties());
    }

    @org.junit.Test
    public void testGetWeather() throws Exception {

    }

    @org.junit.Test
    public void testGetWeatherDayPage() throws Exception {
        when(REQUIRED_CONNECTION.get()).thenReturn(DOCUMENT);
        assertEquals(DOCUMENT, parser.getWeatherDayPage(TEST_DATE));
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testGetWeatherDayPageIOE() throws Exception {
        when(REQUIRED_CONNECTION.get()).thenThrow(new IOException());
        assertEquals(DOCUMENT, parser.getWeatherDayPage(TEST_DATE));
        verify(Jsoup.connect(anyString()), times(6));
    }

    @org.junit.Test
    public void testGetUrlWithDate() throws Exception {
        assertEquals(REQUIRED_URL, parser.getUrlWithDate(URL_PATTERN_STRING, TEST_DATE));
    }*/
}