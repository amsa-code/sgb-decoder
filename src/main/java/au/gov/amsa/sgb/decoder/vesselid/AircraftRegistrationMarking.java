package au.gov.amsa.sgb.decoder.vesselid;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class AircraftRegistrationMarking implements VesselId {

    // used for serialization
    private final VesselIdType vesselIdType = VesselIdType.AIRCRAFT_REGISTRATION_MARKING;
    private final Optional<String> value;

    public AircraftRegistrationMarking(String value) {
        Preconditions.checkNotNull(value);
        String s = value.trim();
        if (s.isEmpty()) {
            this.value = Optional.empty();
        } else {
            this.value = Optional.of(s);
        }
    }

    public VesselIdType vesselIdType() {
        return vesselIdType;
    }

    public Optional<String> value() {
        return value;
    }

}
