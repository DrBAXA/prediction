package com.vdanyliuk.util;

public class Average {
    private double val;
    private int count;

    public Average add(double d) {
        val += d;
        count++;
        return this;
    }

    public double get() {
        return val / count;
    }

    @Override
    public String toString() {
        return "" + get();
    }
}
