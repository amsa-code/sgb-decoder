package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertEquals;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.Test;

public class EltDtInFlightEmergencyTest {

    @Test
    public void testToString() {
        EltDtInFlightEmergency a = new EltDtInFlightEmergency(OffsetTime.of(15, 44, 32, 0, ZoneOffset.UTC), 57,
                TriggeringEvent.MANUAL_ACTIVATION_BY_CREW, GnssStatus.LOCATION_3D,
                Optional.of(Range.min(50).exclusive().build()));
        assertEquals(RotatingFieldType.ELT_DT_IN_FLIGHT_EMERGENCY, a.rotatingFieldType());
    }

}
