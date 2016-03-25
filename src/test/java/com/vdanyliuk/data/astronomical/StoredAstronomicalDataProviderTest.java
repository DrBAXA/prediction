package com.vdanyliuk.data.astronomical;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class StoredAstronomicalDataProviderTest {

    @Spy
    private StoredAstronomicalDataProvider dataProvider;

    private AstronomyData data1;
    private AstronomyData data2;

    @Before
    public void init() {
        //dataProvider = new StoredAstronomicalDataProvider();

        data1 = AstronomyData.builder()
                .astronomicalDayLong(12*60+30)
                .dayLightLong(16*60+8)
                .sunRise(LocalTime.of(6, 11))
                .sunSet(LocalTime.of(18,42))
                .build();

        data2 = AstronomyData.builder()
                .astronomicalDayLong(12*60+34)
                .dayLightLong(16*60+12)
                .sunRise(LocalTime.of(6, 9))
                .sunSet(LocalTime.of(18,43))
                .build();

    }


    @Test
    public void testGetDataWithoutCache() throws Exception {
        TestCase.assertEquals(data1, dataProvider.getData(LocalDate.of(2016, 3, 25)));

        Mockito.verify(dataProvider, times(1)).getFromNet(any(LocalDate.class));
    }

    @Test
    public void testGetDataFromCache() throws Exception {
        TestCase.assertEquals(data1, dataProvider.getData(LocalDate.of(2016, 3, 25)));
        TestCase.assertEquals(data1, dataProvider.getData(LocalDate.of(2016, 3, 25)));
        Mockito.verify(dataProvider, times(1)).getFromNet(any(LocalDate.class));
    }
}