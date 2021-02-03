package au.gov.amsa.sgb.decoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.junit.ComparisonFailure;

import au.gov.amsa.sgb.decoder.internal.Util;
import au.gov.amsa.sgb.decoder.internal.json.Json;

public final class TestingUtil {

    public static String readResource(String resourceName) {
        try (InputStream in = TestingUtil.class.getResourceAsStream(resourceName)) {
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void assertResourceEqualsJson(String resourceName, String json) {
        String expected = readResource(resourceName);
        assertJsonEquals(expected, json);
    }

    public static void assertJsonEquals(String expected, String json) throws ComparisonFailure {
        if (!Json.equals(expected, json)) {
            System.out.println("not expected: " + json);
            throw new ComparisonFailure("unequal json", Json.prettyPrint(expected), Json.prettyPrint(json));
        }
    }

    public static String ones(int n) {
        return Util.repeat('1', n);
    }

    public static String zeros(int n) {
        return Util.repeat('0', n);
    }

}
