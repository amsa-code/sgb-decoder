package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnknownRotatingFieldTest {

    @Test
    public void testToString() {
        UnknownRotatingField a = new UnknownRotatingField("11011");
        assertEquals(RotatingFieldType.UNKNOWN, a.rotatingFieldType());
    }
}
