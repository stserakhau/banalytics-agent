package com.banalytics.box.module.media.task.sound.utils;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.text.DecimalFormat;

public class Spectrogram {
    // https://github.com/bytedeco/javacpp-presets/tree/master/fftw
    private static FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    private static final PearsonsCorrelation PEARSONS_CORRELATION = new PearsonsCorrelation();

    public final Complex[] complexData;
    public final double[] magnitude;
    public final double[] phase;

    public final double[] scaledMagnitude;

    private Spectrogram(Complex[] complexData, double[] magnitude, double[] phase, int scaleToSize) {
        this.complexData = complexData;
        this.magnitude = magnitude;
        this.phase = phase;

        if (scaleToSize < 2) {
            throw new RuntimeException("Invalid argument. compressToSize < 2");
        }
        Spectrogram.recalculate(this);

        {// interested only first half of FFT data magniture
            int dataLen = (magnitude.length >> 1);
//            int dataLen = magnitude.length;
            this.scaledMagnitude = new double[scaleToSize];
//            double measurementsPerUnit = (double) scaleToSize / dataLen;
//            double targetPos = 0;
            int previousTargetIndex = 0;
            int counter = 0;
            int maxIndex = (int) (Math.log10(dataLen) * 10) + 1;
            double scale = (double) scaleToSize / maxIndex;
            for (int i = 1; i < dataLen - 1; i++) {//skip zero
                int targetIndex = (int) (((int) (Math.log10(i) * 10)) * scale);

                if (targetIndex != previousTargetIndex) {
                    scaledMagnitude[previousTargetIndex] /= counter;
                    counter = 0;
                }

                scaledMagnitude[targetIndex] += magnitude[i];
//                targetPos += measurementsPerUnit;
                previousTargetIndex = targetIndex;
                counter++;
            }
            scaledMagnitude[0] = 0;//reset 0 index to 0 (fft issue)
            scaledMagnitude[scaledMagnitude.length - 2] = 0; //reset last index to 0 (fft issue)
            scaledMagnitude[scaledMagnitude.length - 1] = 0; //reset last index to 0 (fft issue)
        }
    }

    /**
     * Data size must be 8, 16, 32, 64, 128, 256, ..., 2^n measurements
     *
     * @param data
     * @return
     */
    public static Spectrogram buildSpectr(short[] data, int numberOfRanges) {
        double[] ddata = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            ddata[i] = data[i];
        }

        return buildSpectr(ddata, numberOfRanges);
    }

    public static Spectrogram buildSpectr(double[] data, int numberOfRanges) {
        Complex[] result = transformer.transform(data, TransformType.FORWARD);
        double[] magnitude = new double[data.length];
        double[] phase = new double[data.length];
        Spectrogram sm = new Spectrogram(result, magnitude, phase, numberOfRanges);
        return sm;
    }

    public static short[] buildSound(Spectrogram spectrogram) {
        Complex[] complexData = spectrogram.complexData;
        Complex[] result = transformer.transform(complexData, TransformType.INVERSE);

        short[] sound = new short[result.length];
        for (int i = 0; i < result.length; i++) {
            sound[i] = (short) result[i].getReal();
        }


        return sound;
    }


    public static double[] prepareData(double[] data) {
        int selectedMeasurementSize = data.length;
        int size = align(selectedMeasurementSize);

        double[] dataArray = new double[size];
        System.arraycopy(data, 0, dataArray, 0, selectedMeasurementSize);

        return dataArray;
    }

    public static short[] prepareData(short[] data) {
        int selectedMeasurementSize = data.length;
        int size = align(selectedMeasurementSize);

        short[] dataArray = new short[size];
        System.arraycopy(data, 0, dataArray, 0, selectedMeasurementSize);

        return dataArray;
    }

    /**
     * @param spectrogram
     * @param splitCount  - value 2^n
     * @return
     */
    public Similarity similarTo(Spectrogram spectrogram, int splitCount) {
        if (splitCount == 1) {
            double magnitude = PEARSONS_CORRELATION.correlation(this.magnitude, spectrogram.magnitude);
            return new Similarity(magnitude);
        }

        int length = spectrogram.magnitude.length; // split to 2 because magnitude is symmetric
        int blockSize = length / splitCount;

        double magnitude = 1;

        double[] buffer1 = new double[blockSize];
        double[] buffer2 = new double[blockSize];

        for (int i = 0; i < length; i += blockSize) {
            System.arraycopy(this.magnitude, i, buffer1, 0, blockSize);
            System.arraycopy(spectrogram.magnitude, i, buffer2, 0, blockSize);
            magnitude *= PEARSONS_CORRELATION.correlation(buffer1, buffer2);
        }

        return new Similarity(magnitude);
    }

    public void substract(Spectrogram value) {
        for (int i = 0; i < magnitude.length; i++) {
            double magOrig = this.magnitude[i];
            double magSubtract = value.magnitude[i];

            double res = magOrig - magSubtract;
            res = res < 0 ? 0 : res;
            this.magnitude[i] = Math.abs(res);

            double phaseOrig = this.phase[i];

            double real = this.magnitude[i] * Math.cos(phaseOrig);
            double imag = this.magnitude[i] * Math.sin(phaseOrig);

            complexData[i] = new Complex(real, imag);
        }

        recalculate(this);
    }

    private static void recalculate(Spectrogram val) {
        for (int i = 0; i < val.complexData.length; i++) {
            Complex x = val.complexData[i];
            val.magnitude[i] = Math.sqrt(x.getReal() * x.getReal() + x.getImaginary() * x.getImaginary());
            val.phase[i] = Math.atan2(x.getImaginary(), x.getReal());
        }
    }

    public static int align(int selectedMeasurementSize) {
        if (selectedMeasurementSize <= 16) {
            return 16;
        } else if (selectedMeasurementSize <= 32) {
            return 32;
        } else if (selectedMeasurementSize <= 64) {
            return 64;
        } else if (selectedMeasurementSize <= 128) {
            return 128;
        } else if (selectedMeasurementSize <= 256) {
            return 256;
        } else if (selectedMeasurementSize <= 512) {
            return 512;
        } else if (selectedMeasurementSize <= 1024) {
            return 1024;
        } else if (selectedMeasurementSize <= 2048) {
            return 2048;
        } else if (selectedMeasurementSize <= 4096) {
            return 4096;
        } else if (selectedMeasurementSize <= 8192) {
            return 8192;
        } else if (selectedMeasurementSize <= 16384) {
            return 16384;
        } else if (selectedMeasurementSize <= 32768) {
            return 32768;
        } else if (selectedMeasurementSize <= 65536 * 2) {
            return 65536 * 2;
        } else if (selectedMeasurementSize <= 65536 * 4) {
            return 65536 * 4;
        } else if (selectedMeasurementSize <= 65536 * 8) {
            return 65536 * 8;
        } else if (selectedMeasurementSize <= 65536 * 16) {
            return 65536 * 16;
        } else if (selectedMeasurementSize <= 65536 * 32) {
            return 65536 * 32;
        }
        throw new RuntimeException("No alignment for " + selectedMeasurementSize);
    }

    public static class Similarity {
        private static final DecimalFormat DF = new DecimalFormat("#.####");
        public final double magnitudeSimilarity;

        public Similarity(double magnitudeSimilarity) {
            this.magnitudeSimilarity = magnitudeSimilarity;
        }

        @Override
        public String toString() {
            return DF.format(magnitudeSimilarity);
        }
    }
}
