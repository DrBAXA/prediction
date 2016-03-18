package com.vdanyliuk.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Class for constructing data model row
 */
@NoArgsConstructor
public class XDataRowBuilder {

    private List<ValueOperatorPair> values = new ArrayList<>();
    private List<BiValueOperatorPair> biValues = new ArrayList<>();

    /**
     * Add result of operation on parameter to data model.
     * @param d data value
     * @param ud operation on this data (i.e. square, cubic)
     * @return this builder
     */
    public XDataRowBuilder addParameter(double d, DoubleUnaryOperator ud) {
        values.add(new ValueOperatorPair(d, ud));
        return this;
    }

    /**
     * Add result of operation on parameters to data model.
     * @param d1 first parameter
     * @param d2 second parameter
     * @param ud binary operator that returns data model parameter
     * @return this builder
     */
    public XDataRowBuilder addBiParameter(double d1, double d2, DoubleBinaryOperator ud) {
        biValues.add(new BiValueOperatorPair(d1, d2, ud));
        return this;
    }

    /**
     * Build data model row from ll set parameters
     * @return double array that represent data model
     */
    @SuppressWarnings("unchecked")
    public double[] buildXArrayRow(){
        return values.stream()
                .mapToDouble(ValueOperatorPair::result)
                .toArray();
    }

    /**
     * @return count of currently added parameters
     */
    public int parametersCount() {
        return values.size() + biValues.size();
    }

    @AllArgsConstructor
    private static class ValueOperatorPair {
        private double val;
        private DoubleUnaryOperator operation;

        public double result() {
            return operation.applyAsDouble(val);
        }
    }

    @AllArgsConstructor
    private static class BiValueOperatorPair {
        private double val1;
        private double val2;
        private DoubleBinaryOperator operation;

        public double result() {
            return operation.applyAsDouble(val1, val2);
        }
    }

}
