package com.banalytics.box.module.toys.pid;

public interface PID {
    void setRange(double minValue, double maxValue);

    void setTargetPoint(double targetPoint);
    double getTargetPoint();

    double getOutput(double input);
}
