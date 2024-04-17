package com.banalytics.box.module.toys.quadrocopter.action;

import java.util.function.Function;

public enum AxisFunction {
    X3((x) -> x * x * x),
    LINEAR((x) -> x);


    private final Function<Double, Double> func;

    AxisFunction(Function<Double, Double> func) {
        this.func = func;
    }

    public double calc(double axisValue) {
        return func.apply(axisValue);
    }
}
