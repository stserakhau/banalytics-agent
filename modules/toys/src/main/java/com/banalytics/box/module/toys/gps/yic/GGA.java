package com.banalytics.box.module.toys.gps.yic;

public record GGA(
        String utcPosition,
        double latitude,
        NSIndicator nsIndicator,
        double longitude,
        EWIndicator ewIndicator,
        int positionFixIndicator,
        int sattelitesUsed,
        double hdop,
        double mslAltitude,
        char mslUnits,
        String geoidSeparation,
        char units2,
        int ageOfDiffCorr,
        String diffRefStationId
) {

}
