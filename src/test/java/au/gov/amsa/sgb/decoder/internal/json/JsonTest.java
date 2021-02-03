package au.gov.amsa.sgb.decoder.internal.json;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class JsonTest {

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(Json.class);
    }

    @Test(expected = RuntimeException.class)
    public void testEqualsWithInvalidJson() {
        assertTrue(Json.equals("boo", "you"));
    }

    @Test(expected = RuntimeException.class)
    public void testToJsonWithNoDefinedSerializer() {
        Json.toJson(new Object());
    }

    @Test(expected = RuntimeException.class)
    public void testPrettyPrintInvalidJson() {
        Json.prettyPrint("boo");
    }

    @Test
    public void testSerializeThingDoesNotIncludeNulls() {
        assertTrue(Json.equals("{\"count\":1}\n" + "", Json.toJson(new Thing())));
    }

    @SuppressWarnings("unused")
    public static final class Thing {
        private Optional<Stuff> stuff = null;
        private int count = 1;
    }

    public enum Stuff {
        BOO, DUH;
    }

}
