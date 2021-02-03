package au.gov.amsa.sgb.decoder.rotatingfield;

import static au.gov.amsa.sgb.decoder.TestingUtil.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.gov.amsa.sgb.decoder.internal.json.Json;

public final class RangeTest {

    @Test
    public void testRange() {
        Range r = Range.min(1).exclusive().max(6).build();
        assertEquals(1, r.min().get().value());
        assertTrue(r.min().get().isExclusive());
        assertEquals(6, r.max().get().value());
        assertFalse(r.max().get().isExclusive());
        // get coverage
        r.hashCode();
    }

    @Test
    public void testRangeToString() {
        {
            Range r = Range.min(1).exclusive().max(6).build();
            assertEquals("Range [min=1, minExclusive=true, max=6, maxExclusive=false]", r.toString());
        }
        {
            Range r = Range.min(1).build();
            assertEquals("Range [min=1, minExclusive=false]", r.toString());
        }
    }

    @Test
    public void testRangeToStringMinNotPresent() {
        Range r = Range.max(6).build();
        assertEquals("Range [max=6, maxExclusive=false]", r.toString());
    }

    @Test
    public void testRangeMissing() {
        Range r = Range.unlimited();
        assertFalse(r.min().isPresent());
        assertFalse(r.max().isPresent());
    }

    @Test
    public void testEquals() {
        Range r = Range.min(1).exclusive().max(6).build();
        assertFalse(r.equals(null));
        assertTrue(r.equals(r));
        assertFalse(r.equals(new Object()));
        assertFalse(r.equals(Range.min(2).exclusive().max(6).build()));
        assertFalse(r.equals(Range.min(1).max(6).build()));
        assertFalse(r.equals(Range.min(2).exclusive().max(7).build()));
        assertFalse(r.equals(Range.min(1).exclusive().max(6).exclusive()));
        assertTrue(r.equals(Range.min(1).exclusive().max(6).build()));
        assertFalse(r.equals(Range.min(1).build()));
        assertFalse(Range.min(1).build().equals(r));
    }

    @Test
    public void testRangeUnlimitedToJson() {
        Range r = Range.unlimited();
        assertJsonEquals("{}", Json.toJson(r));
    }

    @Test
    public void testRangeHashCode() {
        Range.min(1).build().hashCode();
    }

    @Test
    public void testRangeToJson() {
        {
            Range r = Range.min(1).max(2).build();
            assertJsonEquals("{\"min\":{\"value\":1,\"exclusive\":false},\"max\":{\"value\":2,\"exclusive\":false}}",
                    Json.toJson(r));
        }
        {
            Range r = Range.min(1).exclusive().max(2).build();
            assertTrue(r.min().get().isExclusive());
            assertJsonEquals("{\"min\":{\"value\":1,\"exclusive\":true},\"max\":{\"value\":2,\"exclusive\":false}}",
                    Json.toJson(r));
        }
        {
            Range r = Range.max(2).build();
            assertJsonEquals("{\"max\" : {\"value\" : 2, \"exclusive\" : false }}", Json.toJson(r));
        }
        {
            Range r = Range.min(1).max(2).exclusive();
            assertJsonEquals("{\"min\":{\"value\":1,\"exclusive\":false},\"max\":{\"value\":2,\"exclusive\":true}}",
                    Json.toJson(r));
        }
        {
            Range r = Range.min(1).build();
            assertJsonEquals("{\"min\":{\"value\":1,\"exclusive\":false}}", Json.toJson(r));
        }
        {
            Range r = Range.min(1).max(2).build();
            assertJsonEquals("{\"min\":{\"value\":1,\"exclusive\":false},\"max\":{\"value\":2,\"exclusive\":false}}",
                    Json.toJson(r));
        }
    }

}
