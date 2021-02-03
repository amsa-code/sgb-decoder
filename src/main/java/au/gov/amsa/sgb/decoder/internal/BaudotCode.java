package au.gov.amsa.sgb.decoder.internal;

import java.util.HashMap;
import java.util.Map;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

/**
 * A Baudot-like code. Same principle, different mappings.
 */
public final class BaudotCode {

    private static final Map<Integer, Character> map = createMap();

    private BaudotCode() {
        // prevent instantiation
    }

    private static Map<Integer, Character> createMap() {
        Map<Integer, Character> m = new HashMap<>();
        m.put(val("111000"), 'A');
        m.put(val("110011"), 'B');
        m.put(val("101110"), 'C');
        m.put(val("110010"), 'D');
        m.put(val("110000"), 'E');
        m.put(val("110110"), 'F');
        m.put(val("101011"), 'G');
        m.put(val("100101"), 'H');
        m.put(val("101100"), 'I');
        m.put(val("111010"), 'J');
        m.put(val("111110"), 'K');
        m.put(val("101001"), 'L');
        m.put(val("100111"), 'M');
        m.put(val("100110"), 'N');
        m.put(val("100011"), 'O');
        m.put(val("101101"), 'P');
        m.put(val("111101"), 'Q');
        m.put(val("101010"), 'R');
        m.put(val("110100"), 'S');
        m.put(val("100001"), 'T');
        m.put(val("111100"), 'U');
        m.put(val("101111"), 'V');
        m.put(val("111001"), 'W');
        m.put(val("110111"), 'X');
        m.put(val("110101"), 'Y');
        m.put(val("110001"), 'Z');
        m.put(val("100100"), ' ');
        m.put(val("011000"), '-');
        m.put(val("010111"), '/');
        m.put(val("001101"), '0');
        m.put(val("011101"), '1');
        m.put(val("011001"), '2');
        m.put(val("010000"), '3');
        m.put(val("001010"), '4');
        m.put(val("000001"), '5');
        m.put(val("010101"), '6');
        m.put(val("011100"), '7');
        m.put(val("001100"), '8');
        m.put(val("000011"), '9');
        return m;
    }

    private static Integer val(String s) {
        return Integer.parseInt(s, 2);
    }

    public static char toChar(int code) {
        Character ch = map.get(code);
        if (ch == null) {
            throw new IllegalArgumentException("character not found for decimal BaudotCode " + code);
        } else {
            return ch;
        }
    }

    public static char toCharFromShortCode(int shortCode) {
        return toChar(shortCode + 32);
    }

    @VisibleForTesting
    static int size() {
        return map.size();
    }

}
