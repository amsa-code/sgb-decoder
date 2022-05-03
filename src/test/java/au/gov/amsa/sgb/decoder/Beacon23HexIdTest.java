package au.gov.amsa.sgb.decoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Files;

import au.gov.amsa.sgb.decoder.internal.json.Json;
import au.gov.amsa.sgb.decoder.vesselid.AircraftRegistrationMarking;
import au.gov.amsa.sgb.decoder.vesselid.RadioCallSign;

public class Beacon23HexIdTest {

    @Test
    public void testDecode() throws IOException {
        Beacon23HexId b = Beacon23HexId.fromHex("9934039823D000000000000");
        assertEquals(201, b.countryCode());
        assertEquals(230, b.tac());
        assertFalse(b.tacDescription().isPresent());
        assertEquals(573, b.serialNumber());
        assertFalse(b.testProtocolFlag());
        assertFalse(b.vesselId().isPresent());
        TestingUtil.assertJsonEquals(
                "{\"countryCode\":201,\"tac\":230,\"serialNumber\":573,\"testProtocolFlag\":false}", b.toJson());
        File f = new File("src/test/resources/compliance-kit/beacon-23-hex-id-sample.json");
        f.delete();
        Files.write(Json.prettyPrint(b.toJson()).getBytes(StandardCharsets.UTF_8), f);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeHexWrongSize() {
        Beacon23HexId.fromHex("123");
    }

    @Test
    public void testRadioCallSignDecode7Characters() {
        Beacon23HexId b = Beacon23HexId.fromHex("ADF7DA6D7092E33BA475940");
        RadioCallSign r = (RadioCallSign) b.vesselId().get();
        assertEquals("ABC 123", r.value().get());
    }

    @Test
    public void testRadioCallSignDecode6Characters() {
        RadioCallSign r = (RadioCallSign) // 
            Beacon23HexId.fromHex("A037DA6D7092937D7175940").vesselId().get();
        assertEquals("XYZ123", r.value().get());
    }

    @Test
    public void testRegistrationMarking() {
        AircraftRegistrationMarking r = (AircraftRegistrationMarking) //
            Beacon23HexId.fromHex("B5F46E783F23924E9D65028").vesselId().get();
        assertEquals("J1234", r.value().get());
    }

}
