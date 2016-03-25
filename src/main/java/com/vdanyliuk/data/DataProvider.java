package com.vdanyliuk.data;

import java.time.LocalDate;

public interface DataProvider<T> {

    T getData(LocalDate date);

}
