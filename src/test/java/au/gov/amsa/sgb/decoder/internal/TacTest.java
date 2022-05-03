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
        assertEquals("Type Approval Testing", Tac.description(9998).get());
        assertEquals("EPIRB", Tac.description(12000).get());
        assertEquals("ELT", Tac.description(14000).get());
        assertEquals("ELT(DT)", Tac.description(16000).get());
        assertEquals("Reserved - Future Use", Tac.description(18000).get());
        assertEquals("Reserved - System Beacons", Tac.description(65535).get());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadErrors() throws MalformedURLException {
        Tac.loadDescriptions(new URL("http://with space"));
    }

}
