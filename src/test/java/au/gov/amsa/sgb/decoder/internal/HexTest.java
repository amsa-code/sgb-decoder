package au.gov.amsa.sgb.decoder.internal;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class HexTest {

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(Hex.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitStringToHexWrongLength() {
        Hex.bitStringToHex("000");
    }

}
