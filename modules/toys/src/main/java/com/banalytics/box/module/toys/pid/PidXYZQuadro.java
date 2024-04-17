package com.banalytics.box.module.toys.pid;

public class PidXYZQuadro {
    final double xkP;
    final double xkI;
    final double xkD;
    final double ykP;
    final double ykI;
    final double ykD;
    final double zkP;
    final double zkI;
    final double zkD;
    final Range pitchRange;

    final Range headingRange;

    final Range rollRange;

    double targetPitch;
    double targetHeading;
    double rollSpeedGrad;

    public PidXYZQuadro(
            String xPidConfig,
            String yPidConfig,
            String zPidConfig,
            Range pitchRange, Range headingRange, Range rollRange) {
        String[] partsx = xPidConfig.split(";");
        xkP = Double.parseDouble(partsx[0]);
        xkI = Double.parseDouble(partsx[1]);
        xkD = Double.parseDouble(partsx[2]);

        String[] partsy = yPidConfig.split(";");
        ykP = Double.parseDouble(partsy[0]);
        ykI = Double.parseDouble(partsy[1]);
        ykD = Double.parseDouble(partsy[2]);

        String[] partsz = zPidConfig.split(";");
        zkP = Double.parseDouble(partsz[0]);
        zkI = Double.parseDouble(partsz[1]);
        zkD = Double.parseDouble(partsz[2]);

        this.pitchRange = pitchRange;
        this.headingRange = headingRange;
        this.rollRange = rollRange;
    }

    /**
     * Value in provided range
     */
    public void setTargetPitch(double pitch) {
        this.targetPitch = pitch;
    }

    /**
     * Value in provided range
     */
    public void setTargetHeading(double heading) {
        this.targetHeading = heading;
    }

    /**
     * value in provided range
     */
    public void setTargetRollSpeed(double rollSpeedGrad) {
        this.rollSpeedGrad = rollSpeedGrad;
    }

    double[] enginePowerDistribution = new double[4];

    double pitchIntegral, headingIntegral, rollIntegral;

    double prevDPitchErr;
    double prevDHeadingErr;
    double prevDRollErr;

    public double[] engineVectors(double currentPitchGrad, double currentHeadingGrad, double currentRollGrad) {
        double dPitchErr = targetPitch - currentPitchGrad;
        double dpi = xkI * dPitchErr;
        if (pitchRange.isInRange(pitchIntegral + dpi)) {
            pitchIntegral += dpi;
        }
        double dPitch = dPitchErr - prevDPitchErr;
        prevDPitchErr = dPitchErr;
        double pitchOut = dPitchErr * xkP + pitchIntegral + dPitch * xkD;
        double pitchNorm = pitchRange.normalized(pitchOut);

        double dHeadingErr = targetHeading - currentHeadingGrad;
        double dhi = ykI * dHeadingErr;
        if (pitchRange.isInRange(headingIntegral + dhi)) {
            headingIntegral += dhi;
        }
        double dHeading = dHeadingErr - prevDHeadingErr;
        prevDHeadingErr = dHeading;
        double headingOut = dHeadingErr * ykP + headingIntegral + dHeading * ykD;
        double headingNorm = headingRange.normalized(headingOut);

        double dRollErr = rollSpeedGrad;
        double dhr = zkI * dRollErr;
        if (rollRange.isInRange(rollIntegral + dhi)) {
            rollIntegral += dhr;
        }
        double dRoll = dRollErr - prevDRollErr;
        prevDRollErr = dRoll;
        double rollOut = dRollErr * zkP + rollIntegral + dRoll * zkD;
        double rollNorm = rollRange.normalized(rollOut);

//        System.out.printf("=========== (%s / %s) | (%s / %s)%n", pitchOut, pitchNorm, headingOut, headingNorm);

        double kE1 = 0;
        double kE2 = 0;
        double kE3 = 0;
        double kE4 = 0;

        if (rollNorm != 0) {
            kE1 = rollNorm;
            kE4 = rollNorm;
            kE2 = -rollNorm;
            kE3 = -rollNorm;
        }

        if (pitchNorm != 0) {
            if (pitchNorm > 0) {
                kE1 = pitchNorm;
                kE3 = pitchNorm;
            } else {
                kE2 = -pitchNorm;
                kE4 = -pitchNorm;
            }
        }

        if (headingNorm != 0) {
            if (headingNorm > 0) {
                kE3 += headingNorm;
                kE4 += headingNorm;
            } else {
                kE1 += -headingNorm;
                kE2 += -headingNorm;
            }
        }
        enginePowerDistribution[0] = kE1;
        enginePowerDistribution[1] = kE2;
        enginePowerDistribution[2] = kE3;
        enginePowerDistribution[3] = kE4;
        return enginePowerDistribution;
    }

    public record Range(double min, double max) {
        public double normalized(double value) {
            double valueInRange = Math.max(min, Math.min(value, max));
            return 2 * valueInRange / (max - min);
        }

        public boolean isInRange(double value) {
            return min < value && value < max;
        }
    }
}
