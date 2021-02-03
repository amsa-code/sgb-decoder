package au.gov.amsa.sgb.decoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Files;

import au.gov.amsa.sgb.decoder.internal.json.Json;

public class Beacon23HexIdTest {

    @Test
    public void testDecode() throws IOException {
        Beacon23HexId b = Beacon23HexId.fromHex("9934039823D000000000000");
        assertEquals(201, b.countryCode());
        assertEquals(230, b.tac());
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

}
