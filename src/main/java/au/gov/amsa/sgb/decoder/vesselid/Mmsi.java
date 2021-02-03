package au.gov.amsa.sgb.decoder.vesselid;

import java.util.Optional;

public final class Mmsi implements VesselId {

    // used for serialization
    private final VesselIdType vesselIdType = VesselIdType.MMSI;
    private final Optional<Integer> mmsi;
    private final Optional<Integer> epirbMmsi;

    public Mmsi(Optional<Integer> mmsi, Optional<Integer> epirbMmsi) {
        this.mmsi = mmsi;
        this.epirbMmsi = epirbMmsi;
    }

    public VesselIdType vesselIdType() {
        return vesselIdType;
    }

    public Optional<Integer> mmsi() {
        return mmsi;
    }

    public Optional<Integer> epirbMmsi() {
        return epirbMmsi;
    }

}
