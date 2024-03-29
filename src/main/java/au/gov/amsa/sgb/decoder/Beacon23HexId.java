package au.gov.amsa.sgb.decoder;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

import au.gov.amsa.sgb.decoder.internal.Bits;
import au.gov.amsa.sgb.decoder.internal.Tac;
import au.gov.amsa.sgb.decoder.internal.json.Json;
import au.gov.amsa.sgb.decoder.vesselid.VesselId;

/**
 * Decodes a Beacon 23 Hex character ID. Based on C/T.018 Rev 6
 * (May 2020).
 */
public final class Beacon23HexId {

    private final int countryCode;
    private final int tac;
    private final Optional<String> tacDescription;
    private final int serialNumber;
    private final boolean testProtocolFlag;
    private final Optional<VesselId> vesselId;

    private Beacon23HexId(String hex) {
        Preconditions.checkArgument(hex.length() == 23);
        Bits bits = Bits.fromHex(hex);
        bits.skip(1);
        countryCode = bits.readUnsignedInt(10);
        bits.skip(3);
        tac = bits.readUnsignedInt(16);
        tacDescription = Tac.description(tac);
        serialNumber = bits.readUnsignedInt(14);
        testProtocolFlag = bits.readBoolean();
        vesselId = Detection.readVesselId(bits);
    }

    public static Beacon23HexId fromHex(String hex) {
        return new Beacon23HexId(hex);
    }

    public int countryCode() {
        return countryCode;
    }

    public int tac() {
        return tac;
    }
    
    public Optional<String> tacDescription() {
        return tacDescription;
    }

    public int serialNumber() {
        return serialNumber;
    }

    public boolean testProtocolFlag() {
        return testProtocolFlag;
    }

    public Optional<VesselId> vesselId() {
        return vesselId;
    }

    public String toJson() {
        return Json.toJson(this);
    }
}
