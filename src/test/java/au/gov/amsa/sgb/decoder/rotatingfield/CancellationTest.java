package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CancellationTest {

    @Test
    public void testToString() {
        Cancellation a = new Cancellation(DeactivationMethod.MANUAL_DEACTIVATION_BY_USER);
        assertEquals(RotatingFieldType.CANCELLATION, a.rotatingFieldType());
    }
}
