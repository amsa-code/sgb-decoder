package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NationalUseTest {

    @Test
    public void testToString() {
        NationalUse a = new NationalUse("11011");
        assertEquals(RotatingFieldType.NATIONAL_USE, a.rotatingFieldType());
    }
}
