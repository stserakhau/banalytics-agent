package com.banalytics.box.module.utils;

import java.util.List;

public class ConvertionUtils {
    public static byte[] convert(short[] data_) {
        byte[] data = new byte[data_.length * 2];
        for (int i = 0; i < data.length; ) {
            short val = data_[i >> 1];
            data[i] = (byte) val;
            data[i + 1] = (byte) (val >> 8);
            i += 2;
        }
        return data;
    }

    public static short[] convertBigEndian(byte[] sample) {
        short[] data = new short[sample.length / 2];
        int counter = 0;
        for (int i = 0; i < sample.length; i += 2) {
            short val = (short) (((sample[i] & 0xff) << 8) | (sample[i + 1] & 0xff));
            data[counter] = val;
            counter++;
        }

        return data;
    }

    public static short[] convertLittleEndian(byte[] sample) {
        short[] data = new short[sample.length / 2];
        int counter = 0;
        for (int i = 0; i < sample.length; i += 2) {
            short val = (short) (((sample[i] & 0xff)) | (sample[i + 1] & 0xff) << 8);
            data[counter] = val;
            counter++;
        }

        return data;
    }

    public static double[] averageHistoryMagnitude(List<double[]> listOfArrays) {
        double[] avg = new double[listOfArrays.get(0).length];
        for (double[] el : listOfArrays) {
            for (int i = 0; i < el.length; i++) {
                avg[i] += el[i];
            }
        }
        int size = listOfArrays.size();
        for (int i = 0; i < avg.length; i++) {
            avg[i] = avg[i] / size;
        }
        return avg;
    }
}
