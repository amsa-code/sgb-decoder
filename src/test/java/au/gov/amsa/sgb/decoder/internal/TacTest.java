package au.gov.amsa.sgb.decoder.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TacTest {

    @Test(expected = IllegalArgumentException.class)
    public void testMerge() {
        Tac.MERGE_FUNCTION.apply("a", "a");
    }

    @Test
    public void testDescriptions() {
        assertFalse(Tac.description(1).isPresent());
        assertEquals("PLB", Tac.description(10000).get());
        assertEquals("PLB", Tac.description(10001).get());
        assertEquals("TYPE_APPROVAL_TESTING", Tac.description(9998).get());
        assertEquals("EPIRB", Tac.description(12000).get());
        assertEquals("ELT", Tac.description(14000).get());
        assertEquals("ELT_DT", Tac.description(16000).get());
        assertEquals("RESERVED_FUTURE_USE", Tac.description(18000).get());
        assertEquals("RESERVED_SYSTEM_BEACONS", Tac.description(65535).get());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadErrors() throws MalformedURLException {
        Tac.loadDescriptions(new URL("http://with space"));
    }
    
}
