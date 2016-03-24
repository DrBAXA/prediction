package com.vdanyliuk.data;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class MatrixBuilder {

    private List<Pair<Double>> values = new ArrayList<>();

    public MatrixBuilder addParameter(Double d, UnaryOperator<Double> ud) {
        values.add(new Pair<>(d, ud));
        return this;
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
