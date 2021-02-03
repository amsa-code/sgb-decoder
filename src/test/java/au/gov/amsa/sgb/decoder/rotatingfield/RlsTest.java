package au.gov.amsa.sgb.decoder.rotatingfield;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class RlsTest {

    @Test
    public void testToString() {
        Rls a = new Rls(true, false, RlsProvider.GALILEO, Optional.empty());
        assertEquals(RotatingFieldType.RLS, a.rotatingFieldType());
    }
}
