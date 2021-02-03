package au.gov.amsa.sgb.decoder.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;

public final class Hex {

    private Hex() {
        // prevent instantiation
    }

    private static Map<String, String> map = createMap();
    private static Map<String, String> inverse = createInverse();

    private static Map<String, String> createMap() {
        Map<String, String> m = new HashMap<>();
        m.put("0", "0000");
        m.put("1", "0001");
        m.put("2", "0010");
        m.put("3", "0011");
        m.put("4", "0100");
        m.put("5", "0101");
        m.put("6", "0110");
        m.put("7", "0111");
        m.put("8", "1000");
        m.put("9", "1001");
        m.put("A", "1010");
        m.put("B", "1011");
        m.put("C", "1100");
        m.put("D", "1101");
        m.put("E", "1110");
        m.put("F", "1111");
        return m;
    }

    private static Map<String, String> createInverse() {
        return map.entrySet().stream().collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey()));
    }

    public static String hexToBinary(String s) {
        char[] hex = s.toUpperCase(Locale.ENGLISH).toCharArray();
        StringBuilder b = new StringBuilder();
        for (char h : hex) {
            b.append(map.get(String.valueOf(h)));
        }
        return b.toString();
    }

    public static String bitStringToHex(String bitString) {
        Preconditions.checkArgument(bitString.length() % 4 == 0);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < bitString.length(); i += 4) {
            b.append(inverse.get(bitString.substring(i, i + 4)));
        }
        return b.toString();
    }

}
