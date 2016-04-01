package com.vdanyliuk.solver;

import com.vdanyliuk.analize.ChartPloter;
import com.vdanyliuk.data.DependentDataModel;
import com.vdanyliuk.data.IndependentDataModel;
import com.vdanyliuk.data.weather.WeatherModel;
import org.apache.commons.math3.linear.*;

import java.io.IOException;
import java.util.stream.DoubleStream;

public class EnergyLoadWeatherSolver implements RegressionSolver<WeatherModel> {

    private IndependentDataModel<WeatherModel> independentDataModel;
    private DependentDataModel dependentDataModel;
    private boolean isDebudEnabled;

    public EnergyLoadWeatherSolver(IndependentDataModel<WeatherModel> independentDataModel, DependentDataModel dependentDataModel, boolean debug) {
        if (independentDataModel.size() != dependentDataModel.size())
            throw new IllegalArgumentException("Data models should have same rows count");
        this.independentDataModel = independentDataModel;
        this.dependentDataModel = dependentDataModel;
        this.isDebudEnabled = debug;
    }

    @Override
    public double solve(WeatherModel data) {
        int v = 0;

        RealMatrix X = new Array2DRowRealMatrix(independentDataModel.getXArray());//Matrix "m x n" where m - count of data examples, n - count of features
        RealMatrix y = new Array2DRowRealMatrix(dependentDataModel.getYArray()).scalarAdd(-v);// Column vector with row count "m"
        RealVector x = new ArrayRealVector(independentDataModel.getXRow(data));// Row vector with columns count "n"

        RealMatrix R = MatrixUtils.createRealIdentityMatrix(X.getColumnDimension());
        R.setEntry(0, 0, 0);


        //(X^T*X+l*R)*X*y result column vector with rowsCount "n"
        RealMatrix theta = new LUDecomposition((X.transpose().multiply(X)).add(R.scalarMultiply(0))).getSolver().getInverse().multiply(X.transpose()).multiply(y);

        RealMatrix xy = X.multiply(theta).scalarAdd(v);
        RealMatrix dy = xy.add(y.scalarMultiply(-1).scalarAdd(v));
        if (isDebudEnabled) {
            double[] dya = dy.getColumnVector(0).ebeDivide(y.scalarAdd(v).getColumnVector(0)).toArray();
            int allCount = dya.length;
            System.out.println("=======================================");
            System.out.println("squareSum = " + DoubleStream.of(dy.getColumn(0)).map(d -> d * d).sum());
            System.out.println("median = " + DoubleStream.of(dy.getColumn(0)).map(Math::abs).sorted().skip(dy.getRowDimension() / 2).findFirst().orElseGet(() -> 0.0));
            System.out.println("count = " + allCount);
            System.out.printf("max dy = %.3f%n", DoubleStream.of(dya).map(Math::abs).max().orElseGet(() -> 0.0));
            System.out.printf("dy < 2%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.02).count() / (double) allCount * 100);
            System.out.printf("dy < 1%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.01).count() / (double) allCount * 100);
            System.out.printf("dy < 0.5%% count = %.1f%%%n", DoubleStream.of(dya).map(Math::abs).filter(d -> d < 0.005).count() / (double) allCount * 100);
            System.out.println("=======================================");

            try {
                ChartPloter.plotCharts("res.png", y.scalarAdd(v), xy);
                ChartPloter.plotCharts("d.png", dy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //x*theta result 1x1 matrix
        return theta.preMultiply(x).toArray()[0] + v;
    }
}
