package au.gov.amsa.sgb.decoder;

import org.junit.Test;

import au.gov.amsa.sgb.decoder.internal.json.Json;

public class EncodedGnssPositionTest {

    @Test
    public void testToString() {
        EncodedGnssPosition a = new EncodedGnssPosition(-43, 140);
        TestingUtil.assertResourceEqualsJson("/encoded-gnss-position.json", Json.toJson(a));
    }

}
