package au.gov.amsa.sgb.decoder.vesselid;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class MmsiTest {

    @Test
    public void testToString() {
        Mmsi a = new Mmsi(Optional.of(123456789), Optional.of(234567890));
        assertEquals(VesselIdType.MMSI, a.vesselIdType());
    }
}
