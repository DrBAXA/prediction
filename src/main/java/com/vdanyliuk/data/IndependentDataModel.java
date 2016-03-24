package com.vdanyliuk.data;

/**
 * Represents training data we make predictions based on
 *
 * @param <T> base class that is used to build model
 */
public interface IndependentDataModel<T> extends DataModel{

    /**
     * Return data in matrix format,
     * where each row is one data example,
     * each column is one type of data
     *
     * @return data in array format
     */
    double[][] getXArray();

    /**
     * @param data data that will be used to build model
     * @return one row of data
     */
    public double[] getXRow(T data);
}
