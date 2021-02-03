package au.gov.amsa.sgb.decoder.vesselid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AircraftOperatorAndSerialNumberTest {

    @Test
    public void testToString() {
        AircraftOperatorAndSerialNumber a = new AircraftOperatorAndSerialNumber("ABC", 1234);
        assertEquals(VesselIdType.AIRCRAFT_OPERATOR_AND_SERIAL_NUMBER, a.vesselIdType());
    }
}
