package au.gov.amsa.sgb.decoder.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.gov.amsa.sgb.decoder.DetectionTest;

public final class BitsTest {

    @Test
    public void testReadUnsignedInt() {
        Bits b = Bits.from("10110001");
        assertEquals(5, b.readUnsignedInt(3));
        assertEquals(17, b.readUnsignedInt(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromBadBits() {
        Bits.from("1001A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadPositionNegative() {
        Bits.from("111").position(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadPositionTooHigh() {
        Bits.from("111").position(4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkipNegative() {
        Bits.from("111").skip(-1);
    }

    @Test
    public void testEof() {
        Bits b = Bits.from("111");
        assertFalse(b.atEnd());
        b.skip(1);
        assertFalse(b.atEnd());
        b.skip(2);
        assertTrue(b.atEnd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntZeroLength() {
        Bits.from("111").readUnsignedInt(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntPastEnd() {
        Bits.from("111").readUnsignedInt(5);
    }

    @Test
    public void testReadBaudotShort() {
        Bits b = Bits.from("1100010011");
        assertEquals("AB", b.readBaudotCharactersShort(2));
    }

    @Test
    public void testReadBaudot() {
        Bits b = Bits.from("111000110011");
        assertEquals("AB", b.readBaudotCharacters(2));
    }

    @Test
    public void readBoolean() {
        Bits b = Bits.from("101");
        assertTrue(b.readBoolean());
        assertFalse(b.readBoolean());
        assertTrue(b.readBoolean());
        assertTrue(b.atEnd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readBooleanPastEnd() {
        Bits b = Bits.from("101").skip(3);
        b.readBoolean();
    }

    @Test
    public void testToHex() {
        Bits b = Bits.from("01101010");
        assertEquals("6A", b.readHex(2));
    }

    @Test
    public void testEqualsBits() {
        assertFalse(Bits.from("11").isEqualTo(null));
        assertFalse(Bits.from("11").isEqualTo(Bits.from("10")));
        assertFalse(Bits.from("11").isEqualTo(Bits.from("111")));
        assertTrue(Bits.from("11").isEqualTo(Bits.from("11")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBooleanArrayNumBitsZero() {
        Bits.from("11").readBooleanArray(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBooleanArrayNumBitsTooBig() {
        Bits.from("11").readBooleanArray(3);
    }

    @Test
    public void testReadBooleanArray() {
        Bits b = Bits.from("1110001");
        {
            boolean[] a = b.readBooleanArray(3);
            assertArrayEquals(new boolean[] { true, true, true }, a);
        }
        {
            boolean[] a = b.readBooleanArray(4);
            assertArrayEquals(new boolean[] { false, false, false, true }, a);
        }
    }

    @Test
    public void testIsZero() {
        assertFalse(Bits.from("0001").isZero());
        assertFalse(Bits.from("11111").isZero());
        assertTrue(Bits.from("0000").isZero());
    }

    @Test
    public void testPosition() {
        Bits b = Bits.from("1111");
        assertEquals(0, b.position());
        b.skip(1);
        assertEquals(1, b.position());
    }

    @Test
    public void testReadHex24Bit() {
        assertEquals("AC82EC", Bits.from("101011001000001011101100").readHex(6));
    }

    @Test
    public void testBitsConcat() {
        assertEquals("111000", Bits.from("111").concatWith("000").toBitString());
    }

    @Test
    public void testTrimLeadingZeros() {
        assertEquals("11", Bits.from("00011").trimLeadingZeros().toBitString());
        assertEquals("0", Bits.from("0000").trimLeadingZeros().toBitString());
        assertEquals("11111", Bits.from("00000000000011111").trimLeadingZeros().toBitString());
    }

    @Test
    public void testRemainderOnPolynomialDivision() {
        Bits a = Bits.from("10010011100101101");
        Bits b = Bits.from("11010010101");
        assertEquals("11111", a.remainderOnPolynomialDivision(b).trimLeadingZeros().toBitString());
    }

    @Test
    public void testRemainderOnPolynomialDivisionSimple() {
        {
            Bits a = Bits.from("1");
            Bits b = Bits.from("1");
            assertEquals("0", a.remainderOnPolynomialDivision(b).trimLeadingZeros().toBitString());
        }
        {
            Bits a = Bits.from("0111");
            Bits b = Bits.from("0111");
            assertEquals("0", a.remainderOnPolynomialDivision(b).trimLeadingZeros().toBitString());
        }
    }

    @Test
    public void testRemainderOnPolynomialDivisionUsingSpecExample() {
        // From Appendix B4
        Bits bits = Bits.from(DetectionTest.BITS).concatWith(Bits.zeros(48));
        Bits divisor = Bits.from("1110001111110101110000101110111110011110010010111");
        assertEquals("10010010010101001001111110001010111101001001001",
                bits.remainderOnPolynomialDivision(divisor).trimLeadingZeros().toBitString());
    }

    @Test
    public void testBits() {
        Bits b = Bits.from("10000").replace(2, Bits.from("11"));
        assertEquals("10110", b.toBitString());
    }

    @Test
    public void testToString() {
        assertEquals("101", Bits.from("101").toString());
    }

    @Test
    public void testToHexWithLeadingZeros() {
        assertEquals("00", Bits.from("00000000").toHex());
    }

}
