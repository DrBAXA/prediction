package com.vdanyliuk.model;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class MatrixBuilder {

    private List<Pair<Double>> values = new ArrayList<>();
    private Double load;

    public MatrixBuilder(Double load) {
        this.load = load;
    }

    public MatrixBuilder addParameter(Double d, UnaryOperator<Double> ud) {
        values.add(new Pair<>(d, ud));
        return this;
    }

    public String build() {
        return load + "," + values.stream()
                .map(Pair::result)
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public double[] buildXArrayRow(){
        return values.stream()
                .map(Pair::result)
                .mapToDouble(d -> d)
                .toArray();
    }

    @AllArgsConstructor
    private static class Pair<T> {
        private T val;
        private UnaryOperator<T> operation;

        public T result() {
            return operation.apply(val);
        }
    }

}
