package au.gov.amsa.sgb.decoder.internal;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Util {

    private Util() {
        // prevent instantiation
    }

    public static String repeat(char ch, int n) {
        return IntStream.range(0, n).mapToObj(x -> String.valueOf(ch)).collect(Collectors.joining());
    }

    public static String quoted(String s) {
        return "\"" + s + "\"";
    }
}
