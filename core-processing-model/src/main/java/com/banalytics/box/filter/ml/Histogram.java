package com.banalytics.box.filter.ml;

import lombok.Getter;

@Getter
public class Histogram {
    int min;
    int max;
    int columnsCount;

    final int[] columnStatistic;
    final double[] columnNormalized;
    final double range;
    final double columnsWidth;

    int totalValues;

    public Histogram(int min, int max, int columnsCount) {
        if (columnsCount < 2) {
            throw new RuntimeException("Min column count must be 2 or more");
        }
        if (min >= max) {
            throw new RuntimeException("Max value must be grate than min");
        }
        this.min = min;
        this.max = max;
        this.columnsCount = columnsCount;
        this.columnStatistic = new int[columnsCount];
        this.columnNormalized = new double[columnsCount];
        this.range = max - min;
        this.columnsWidth = range / columnsCount;
        this.totalValues = 0;
    }

    public void putValue(int value) {
        if (value < this.min) {
            return;
        }
        this.totalValues++;
        int arrangeVal2Left = value - this.min;
        int index = (int) (arrangeVal2Left / this.columnsWidth);
        if (index > this.columnStatistic.length - 1) {
            index = this.columnStatistic.length - 1;
        }
        this.columnStatistic[index]++;
    }

    public void refreshNormalizedValues() {
        for (int i = 0; i < columnStatistic.length; i++) {
            columnNormalized[i] = columnStatistic[i] / (double) totalValues;
        }
    }

    public void reset() {
        totalValues = 0;
        multiply(0.01);
        for (int j : columnStatistic) {
            totalValues += j;
        }
    }

    public void multiply(double mult) {
        for (int i = 0; i < columnStatistic.length; i++) {
            columnStatistic[i] *= mult;
        }
    }

    /**
     * Exclude first {percent} values from the left and return minimum available values
     */
    public double minWithExclusion(double percent) {
        this.refreshNormalizedValues();

        double percentCounter = 0;
        int index = columnNormalized.length - 1;
        for (int i = 0; i < columnNormalized.length; i++) {
            percentCounter += columnNormalized[i];
            if (percentCounter > percent) {
                index = i;
                break;
            }
        }

        return min + index * columnsWidth;
    }

    public static void main(String[] args) {
        Histogram h = new Histogram(50, 100, 10);
        h.putValue(0);
        h.putValue(10);
        h.putValue(20);
        h.putValue(30);
        h.putValue(40);
        h.putValue(50);
        h.putValue(60);
        h.putValue(70);
        h.putValue(80);
        h.putValue(90);
        h.putValue(100);
        h.putValue(110);

        double minValue = h.minWithExclusion(0.76);

        System.out.println();
    }
}
