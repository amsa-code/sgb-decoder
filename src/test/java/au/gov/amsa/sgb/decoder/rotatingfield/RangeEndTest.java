package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RangeEndTest {

    @Test
    public void testEquals() {
        RangeEnd r = new RangeEnd(0, true);
        assertFalse(r.equals(null));
        assertTrue(r.equals(r));
        assertFalse(r.equals(new RangeEnd(0, false)));
        assertFalse(r.equals(new RangeEnd(1, true)));
        assertFalse(r.equals(new RangeEnd(2, false)));
        assertFalse(r.equals(new Object()));
    }

}
