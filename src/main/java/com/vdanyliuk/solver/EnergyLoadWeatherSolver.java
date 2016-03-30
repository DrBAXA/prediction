package com.vdanyliuk.solver;

import com.vdanyliuk.analize.ChartPloter;
import com.vdanyliuk.data.DependentDataModel;
import com.vdanyliuk.data.IndependentDataModel;
import com.vdanyliuk.data.weather.WeatherModel;
import org.apache.commons.math3.linear.*;

import java.io.IOException;

public class EnergyLoadWeatherSolver implements RegressionSolver<WeatherModel>{

    IndependentDataModel<WeatherModel> independentDataModel;
    DependentDataModel dependentDataModel;

    public EnergyLoadWeatherSolver(IndependentDataModel<WeatherModel> independentDataModel, DependentDataModel dependentDataModel) {
        if(independentDataModel.size() != dependentDataModel.size())
            throw new IllegalArgumentException("Data models should have same rows count");
        this.independentDataModel = independentDataModel;
        this.dependentDataModel = dependentDataModel;
    }

    @Override
    public double solve(WeatherModel data) {
        RealMatrix X = new Array2DRowRealMatrix(independentDataModel.getXArray());//Matrix "m x n" where m - count of data examples, n - count of features
        RealMatrix y = new Array2DRowRealMatrix(dependentDataModel.getYArray());// Column vector with row count "m"
        RealVector x = new ArrayRealVector(independentDataModel.getXRow(data));// Row vector with columns count "n"

        RealMatrix R = MatrixUtils.createRealIdentityMatrix(X.getColumnDimension());
        R.setEntry(0,0,0);

        //(X^T*X+l*R)*X*y result column vector with rowsCount "n"
        RealMatrix theta = new LUDecomposition((X.transpose().multiply(X)).add(R.scalarMultiply(800))).getSolver().getInverse().multiply(X.transpose()).multiply(y);

        RealMatrix xy = X.multiply(theta);
        RealMatrix dy = xy.add(y.scalarMultiply(-1)).scalarMultiply(0.01);

        try {
            ChartPloter.plotCharts("res.png", y, xy);
            ChartPloter.plotCharts("d.png", dy);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //x*theta result 1x1 matrix
        return theta.preMultiply(x).toArray()[0];
    }
}
