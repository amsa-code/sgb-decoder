package au.gov.amsa.sgb.decoder.vesselid;

public final class AircraftOperatorAndSerialNumber implements VesselId {

    // used for serialization
    private final VesselIdType vesselIdType = VesselIdType.AIRCRAFT_OPERATOR_AND_SERIAL_NUMBER;
    private final String aircraftOperatorDesignator;
    private final int serialNumber;

    public AircraftOperatorAndSerialNumber(String aircraftOperatorDesignator, int serialNumber) {
        this.aircraftOperatorDesignator = aircraftOperatorDesignator;
        this.serialNumber = serialNumber;
    }

    public VesselIdType vesselIdType() {
        return vesselIdType;
    }

    public String aircraftOperatorDesignator() {
        return aircraftOperatorDesignator;
    }

    public int serialNumber() {
        return serialNumber;
    }

}
