package com.vdanyliuk.data.load;

import com.vdanyliuk.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class LoadDataProvider implements DataProvider<Double> {

    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public HashMap<LocalDate, Double> data;

    public LoadDataProvider(String file) throws IOException {
        data = getLoad(file);
    }

    private LoadDataProvider(HashMap<LocalDate, Double> data) {
        this.data = data;
    }

    public LoadDataProvider cloneExcept(LocalDate date) {
        if(data.containsKey(date)) {
            Double val = data.remove(date);
            LoadDataProvider provider = new LoadDataProvider((HashMap<LocalDate, Double>)data.clone());
            data.put(date, val);
            return provider;
        } else return this;
    }

    @Override
    public Double getData(LocalDate date) {
        return data.get(date);
    }

    public boolean contains(LocalDate date) {
        return data.containsKey(date);
    }

    private HashMap<LocalDate, Double> getLoad(String file) throws IOException {
        return Files.lines(Paths.get(file))
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, Double>::new, (m, a) -> m.put(LocalDate.parse(a[0], F2), Double.valueOf(a[1])), HashMap::putAll);
    }
}
