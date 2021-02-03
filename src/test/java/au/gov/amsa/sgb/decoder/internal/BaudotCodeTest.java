package au.gov.amsa.sgb.decoder.internal;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class BaudotCodeTest {

    @Test
    public void testConvertBitString() {
        assertEquals(24, toInt("11000"));
    }

    @Test
    public void testBaudotCode() {
        assertEquals('A', BaudotCode.toChar(toInt("111000")));
        assertEquals('B', BaudotCode.toChar(toInt("110011")));
        assertEquals('C', BaudotCode.toChar(toInt("101110")));
        assertEquals('Z', BaudotCode.toChar(toInt("110001")));
        assertEquals('8', BaudotCode.toChar(toInt("001100")));
    }

    @Test
    public void testHasRightNumberOfEntries() {
        assertEquals(39, BaudotCode.size());
    }

    @Test
    public void testIsUtilityClass() {
        Asserts.assertIsUtilityClass(BaudotCode.class);
    }

    @Test
    public void testShortBaudotCode() {
        assertEquals('A', BaudotCode.toCharFromShortCode(toInt("11000")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentCodeThrows() {
        BaudotCode.toChar(1000000);
    }

    private static int toInt(String bitString) {
        return new BigInteger(bitString, 2).intValue();
    }

}
