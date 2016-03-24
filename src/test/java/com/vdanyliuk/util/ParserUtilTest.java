package com.vdanyliuk.util;

import org.junit.Test;

import static com.vdanyliuk.util.ParserUtil.getMinutesValueForCssAndRegex;
import static com.vdanyliuk.util.ParserUtil.getValueForCssAndRegex;
import static com.vdanyliuk.data.weather.WUndergroundWeatherDataProviderTest.DOCUMENT;
import static junit.framework.TestCase.assertEquals;

public class ParserUtilTest {

    @Test
    public void testGetStringByPattern() throws Exception {
        assertEquals("8h 19m", ParserUtil.getStringByPattern("Тривалість дня \t\n8h 19m", "(\\d+h\\s?\\d+m)", 1));
    }

    @Test(expected = PatternMatchingException.class)
    public void testGetStringByPatternException() throws Exception {
        assertEquals("8h 19m", ParserUtil.getStringByPattern("Тривалість дня \t\n8h 19m", "(^\\d+h\\s?\\d+m)", 1));
    }

    @Test
    public void getValueForCssAndRegexTest() {
        assertEquals(60.0, getValueForCssAndRegex(DOCUMENT, "table#historyTable.responsive.airport-history-summary-table tbody tr", "Minimum Humidity\\s*(\\-?\\d+\\.?\\d*)"));
    }

    @Test
    public void getMinutesValueForCssAndRegexTest() {
        assertEquals(573.0, getMinutesValueForCssAndRegex(DOCUMENT, "div#astronomy-mod.wx-module.simple table tbody tr", "Тривалість видимого світла\\s*(\\-?\\d+\\.?\\d*h\\s?\\-?\\d+\\.?\\d*m)", "(\\-?\\d+\\.?\\d*)h",  "(\\-?\\d+\\.?\\d*)m"));
    }
}