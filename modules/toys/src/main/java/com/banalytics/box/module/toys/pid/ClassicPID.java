package com.banalytics.box.module.toys.pid;

public class ClassicPID implements PID {
    double kP;
    double kI;
    double kD;

    double dt = 20;//each teak

    double minValue;
    double maxValue;

    public ClassicPID(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public ClassicPID(String pidVal) {
        String[] parts = pidVal.split(";");
        kP = Double.parseDouble(parts[0]);
        kI = Double.parseDouble(parts[1]);
        kD = Double.parseDouble(parts[2]);
    }

    public void setRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    double targetPoint;

    double prevErr = 0;
    double integral;

    @Override
    public void setTargetPoint(double targetPoint) {
        this.targetPoint = targetPoint;
    }

    @Override
    public double getTargetPoint() {
        return targetPoint;
    }

    public double getOutput(double input) {
        double err = targetPoint - input;

        double di = err * dt * kI;// change integral value only in target range
        if (minValue < integral + di && integral + di < maxValue) {
            integral = integral + di;
        }
        System.out.println(integral);
        double D = (err - prevErr) / dt;
        prevErr = err;

        double out = err * kP + integral + D * kD;

        return Math.max(minValue, Math.min(out, maxValue));
    }

}
