package au.gov.amsa.sgb.decoder.vesselid;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class RadioCallSign implements VesselId {

    // used for serialization
    private final VesselIdType vesselIdType = VesselIdType.RADIO_CALL_SIGN;
    private final Optional<String> value;

    public RadioCallSign(String value) {
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
