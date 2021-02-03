package au.gov.amsa.sgb.decoder.vesselid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RadioCallSignTest {

    @Test
    public void testToString() {
        RadioCallSign a = new RadioCallSign("FLIGHT");
        assertEquals(VesselIdType.RADIO_CALL_SIGN, a.vesselIdType());
    }
}
