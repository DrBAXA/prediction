package com.vdanyliuk;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Holidays {

    private static final Map<LocalDate, Double> STATE_HOLIDAYS = new HashMap<LocalDate, Double>(){{
        put(LocalDate.of(2015, 1, 1), 1.0);
        put(LocalDate.of(2015, 1, 7), 1.0);
        put(LocalDate.of(2015, 3, 9), 1.0);
        put(LocalDate.of(2015, 4, 13), 1.0);
        put(LocalDate.of(2015, 5, 1), 1.0);
        put(LocalDate.of(2015, 5, 4), 1.0);
        put(LocalDate.of(2015, 5, 11), 1.0);
        put(LocalDate.of(2015, 6, 29), 1.0);
        put(LocalDate.of(2015, 8, 24), 1.0);
        put(LocalDate.of(2015, 10, 14), 1.0);
        put(LocalDate.of(2016, 1, 1), 1.0);
        put(LocalDate.of(2016, 1, 7), 1.0);
        put(LocalDate.of(2016, 3, 8), 1.0);
    }};

    private static final Map<LocalDate, Double> SCHOOL_HOLIDAYS = new HashMap<LocalDate, Double>(){{
        put(LocalDate.of(2015, 1, 1), 1.0);
        put(LocalDate.of(2015, 1, 2), 1.0);
        put(LocalDate.of(2015, 1, 3), 1.0);
        put(LocalDate.of(2015, 1, 4), 1.0);
        put(LocalDate.of(2015, 1, 5), 1.0);
        put(LocalDate.of(2015, 1, 6), 1.0);
        put(LocalDate.of(2015, 1, 7), 1.0);
        put(LocalDate.of(2015, 1, 8), 1.0);
        put(LocalDate.of(2015, 1, 9), 1.0);
        put(LocalDate.of(2015, 1, 10), 1.0);
        put(LocalDate.of(2015, 1, 11), 1.0);
        put(LocalDate.of(2015, 1, 12), 1.0);
        put(LocalDate.of(2015, 1, 1), 1.0);
        put(LocalDate.of(2015, 1, 2), 1.0);
        put(LocalDate.of(2015, 1, 3), 1.0);
        put(LocalDate.of(2015, 1, 4), 1.0);
    }};

    private static final Map<LocalDate, Double> RELIGIOUS_HOLIDAYS = new HashMap<LocalDate, Double>(){{
        put(LocalDate.of(2015, 3, 30), 1.0);
        put(LocalDate.of(2015, 4, 7), 2.0);
        put(LocalDate.of(2015, 4, 10), 1.0);
        put(LocalDate.of(2015, 4, 12), 2.0);
        put(LocalDate.of(2015, 4, 13), 2.0);
        put(LocalDate.of(2015, 4, 14), 2.0);
        put(LocalDate.of(2015, 5, 6), 2.0);
        put(LocalDate.of(2015, 5, 21), 1.0);
        put(LocalDate.of(2015, 5, 21), 1.0);
        put(LocalDate.of(2015, 7, 7), 1.0);
        put(LocalDate.of(2015, 7, 12), 1.0);
        put(LocalDate.of(2015, 8, 2), 1.0);
        put(LocalDate.of(2015, 8, 19), 1.0);
        put(LocalDate.of(2015, 8, 28), 1.0);
        put(LocalDate.of(2015, 9, 21), 1.0);
        put(LocalDate.of(2015, 9, 27), 1.0);
        put(LocalDate.of(2015, 11, 21), 2.0);
        put(LocalDate.of(2015, 12, 13), 1.0);
        put(LocalDate.of(2015, 12, 19), 1.0);
        put(LocalDate.of(2015, 12, 25), 2.0);
        put(LocalDate.of(2015, 12, 26), 1.0);
        put(LocalDate.of(2016, 1, 1), 2.0);
        put(LocalDate.of(2016, 1, 6), 2.0);
        put(LocalDate.of(2016, 1, 7), 2.0);
        put(LocalDate.of(2016, 1, 8), 2.0);
        put(LocalDate.of(2016, 1, 9), 1.0);
        put(LocalDate.of(2016, 1, 14), 2.0);
        put(LocalDate.of(2016, 1, 19), 2.0);
        put(LocalDate.of(2016, 1, 20), 1.0);
        put(LocalDate.of(2016, 1, 18), 1.0);
        put(LocalDate.of(2016, 2, 12), 1.0);
        put(LocalDate.of(2016, 2, 15), 1.0);
    }};

    public Double religious(LocalDate date){
        if(date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) return 1.0;
        return Optional.ofNullable(RELIGIOUS_HOLIDAYS.get(date)).orElse(0.0);
    }

    public Double state(LocalDate localDate){
        if(STATE_HOLIDAYS.containsKey(localDate)) return 1.0;
        return isWeekEnd(localDate);
    }

    public Double school(LocalDate localDate){
        if(SCHOOL_HOLIDAYS.containsKey(localDate)) return 1.0;
        //if(localDate.isAfter(LocalDate.of(2015,5,30)) || localDate.isAfter(LocalDate.of(2015,9,1))) return 1.0;
        if(localDate.isAfter(LocalDate.of(2015,10,30)) || localDate.isAfter(LocalDate.of(2015,11,8))) return 1.0;
        if(localDate.isAfter(LocalDate.of(2015,12,30)) || localDate.isAfter(LocalDate.of(2016,1,30))) return 1.0;
        return isWeekEnd(localDate);
    }

    private double isWeekEnd(LocalDate localDate) {
        switch (localDate.getDayOfWeek()){
            case MONDAY: return 0.0;
            case TUESDAY: return 0.0;
            case WEDNESDAY:return 0.0;
            case THURSDAY:return 0.0;
            case FRIDAY:return 0.1;
            case SATURDAY:return 1.0;
            case SUNDAY:return 1.0;
            default:return 0.0;
        }
    }

}
