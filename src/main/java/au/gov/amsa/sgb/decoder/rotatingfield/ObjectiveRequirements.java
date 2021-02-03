package au.gov.amsa.sgb.decoder.rotatingfield;

import java.util.Optional;

public final class ObjectiveRequirements implements RotatingField {

    // used for serialization
    private final RotatingFieldType rotatingFieldType = RotatingFieldType.OBJECTIVE_REQUIREMENTS;
    private final int elapsedTimeSinceActivationHours;
    private final int timeSinceLastEncodedLocationMinutes;
    private final int altitudeEncodedLocationMetres;
    private final Optional<Range> dilutionPrecisionHdop;
    private final Optional<Range> dilutionPrecisionVdop;
    private final ActivationMethod activationMethod;
    private final Optional<Range> remainingBatteryCapacityPercent;
    private final GnssStatus gnssStatus;

    public ObjectiveRequirements(int elapsedTimeSinceActivationHours, int timeSinceLastEncodedLocationMinutes,
            int altitudeEncodedLocationMetres, Optional<Range> dilutionPrecisionHdop,
            Optional<Range> dilutionPrecisionVdop, ActivationMethod activationMethod,
            Optional<Range> remainingBatteryCapacityPercent, GnssStatus gnssStatus) {
        this.elapsedTimeSinceActivationHours = elapsedTimeSinceActivationHours;
        this.timeSinceLastEncodedLocationMinutes = timeSinceLastEncodedLocationMinutes;
        this.altitudeEncodedLocationMetres = altitudeEncodedLocationMetres;
        this.dilutionPrecisionHdop = dilutionPrecisionHdop;
        this.dilutionPrecisionVdop = dilutionPrecisionVdop;
        this.activationMethod = activationMethod;
        this.remainingBatteryCapacityPercent = remainingBatteryCapacityPercent;
        this.gnssStatus = gnssStatus;
    }

    public RotatingFieldType rotatingFieldType() {
        return rotatingFieldType;
    }

    public int elapsedTimeSinceActivationHours() {
        return elapsedTimeSinceActivationHours;
    }

    public int timeSinceLastEncodedLocationMinutes() {
        return timeSinceLastEncodedLocationMinutes;
    }

    public int altitudeEncodedLocationMetres() {
        return altitudeEncodedLocationMetres;
    }

    public Optional<Range> dilutionPrecisionHdop() {
        return dilutionPrecisionHdop;
    }

    public Optional<Range> dilutionPrecisionDop() {
        return dilutionPrecisionVdop;
    }

    public ActivationMethod activationMethod() {
        return activationMethod;
    }

    public Optional<Range> remainingBatteryCapacityPercent() {
        return remainingBatteryCapacityPercent;
    }

    public GnssStatus gnssStatus() {
        return gnssStatus;
    }

}
