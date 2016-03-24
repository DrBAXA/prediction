package com.vdanyliuk.data;

/**
 * Represent right answers training data.
 *
 */
public interface DependentDataModel extends DataModel{

    /**
     *
     * @return one column matrix
     */
    double[][] getYArray();
}
