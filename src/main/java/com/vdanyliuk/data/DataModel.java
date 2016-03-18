package com.vdanyliuk.data;

import java.util.Map;
import java.util.SortedMap;

public class DataModel {

    private Map<XDataRowBuilder, Double> builders;

    private double[][] XData;
    private double[][] YData;

    /**
     * Creates DataModel from {@param builders};
     */
    public DataModel(SortedMap<XDataRowBuilder, Double> builders) {
        this.builders = builders;
    }

    public double[][] getXData() {
        return builders.keySet().stream()
                .map(XDataRowBuilder::buildXArrayRow)
                .toArray(double[][]::new);
    }

    public double[][] getYData() {
        return builders.keySet().stream()
                .map(key -> builders.get(key))
                .map(d -> new double[]{d})
                .toArray(double[][]::new);
    }


}
