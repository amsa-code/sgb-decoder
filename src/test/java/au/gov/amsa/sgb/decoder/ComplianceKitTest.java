package au.gov.amsa.sgb.decoder;

import static au.gov.amsa.sgb.decoder.TestingUtil.assertJsonEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.google.common.io.Files;

import au.gov.amsa.sgb.decoder.internal.Bits;
import au.gov.amsa.sgb.decoder.internal.Util;
import au.gov.amsa.sgb.decoder.internal.json.Json;

/**
 * Runs Compliance Kit tests and creates a draft Compliance Kit in target.
 *
 */
public class ComplianceKitTest {

    @Test
    public void runAllComplianceTests() throws IOException {
        runAllComplianceTestsInFolder(new File("src/test/resources/compliance-kit"));
    }

    @Test
    public void testCreateComplianceKitInTargetFolder() throws ComparisonFailure, IOException {
        File kit = new File("target/compliance-kit");
        kit.mkdirs();
        Arrays.stream(kit.listFiles()).forEach(File::delete);

        List<KitTest> tests = kitTests().collect(Collectors.toList());

        for (KitTest test : tests) {
            write(kit, test);
        }
        // tests should all pass
        runAllComplianceTestsInFolder(kit);
    }

    private static void write(File kit, KitTest t) {
        final String json;
        if (t.type == TestType.DETECTION) {
            Detection d = Detection.fromHexGroundSegmentRepresentation(t.hex);
            json = Json.prettyPrint(d.toJson());
        } else {
            Beacon23HexId b = Beacon23HexId.fromHex(t.hex);
            json = Json.prettyPrint(b.toJson());
        }
        try {
            Files.write(json.getBytes(StandardCharsets.UTF_8), new File(kit, t.filename));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<KitTest> kitTests() throws IOException {
        File file = new File("src/test/resources/compliance-kit/tests.csv");
        List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);
        return lines.stream() //
                .filter(line -> !line.startsWith("#")) //
                .skip(1) //
                .map(line -> line.trim()) //
                .filter(line -> !line.isEmpty()) //
                .map(line -> {
                    String[] items = line.split(",");
                    assertEquals(4, items.length);
                    String type = removeQuotes(items[0]).trim();
                    String title = removeQuotes(items[1]).trim();
                    String hex = removeQuotes(items[2]).trim();
                    String filename = removeQuotes(items[3]).trim();
                    return new KitTest(TestType.fromName(type), title, hex, filename);
                });
    }

    private static void runAllComplianceTestsInFolder(File base) throws IOException, ComparisonFailure {
        kitTests() //
                .forEach(test -> {
                    String json;
                    try {
                        json = Files.readLines(new File(base, test.filename), StandardCharsets.UTF_8) //
                                .stream() //
                                .collect(Collectors.joining("\n"));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (test.type.name().equalsIgnoreCase("Detection")) {
                        Detection d = Detection.fromHexGroundSegmentRepresentation(test.hex);
                        assertJsonEquals(json, d.toJson());
                    } else {
                        Beacon23HexId b = Beacon23HexId.fromHex(test.hex);
                        assertJsonEquals(json, b.toJson());
                    }
                });
    }

    private enum TestType {
        DETECTION("Detection"), BEACON_23_HEX_ID("Beacon 23 Hex Id");

        private final String name;

        TestType(String name) {
            this.name = name;
        }

        static TestType fromName(String name) {
            for (TestType t : TestType.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("not found: " + name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static final class KitTest {

        private final TestType type;
        private final String title;
        private final String hex;
        private final String filename;

        private KitTest(TestType type, String title, String hex, String filename) {
            this.type = type;
            this.title = title;
            this.hex = hex;
            this.filename = filename;
        }

        public void addTo(File kit, List<KitTest> tests) {
            tests.add(this);
            write(kit, this);
        }

        public static Builder type(TestType type) {
            return new Builder(type);
        }

        public static class Builder {
            private TestType type;
            private String title;
            private String hex;

            Builder(TestType type) {
                this.type = type;
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder hex(String hex) {
                this.hex = hex;
                return this;
            }

            public Builder hex(Bits bits) {
                this.hex = Bits.from("00").concatWith(bits).toHex();
                return this;
            }

            public KitTest filename(String filename) {
                return new KitTest(type, title, hex, filename);
            }
        }

        private static String escape(String s) {
            return s.replaceAll("\"", "\"\"");
        }

        public String toCsvLine() {
            return Util.quoted(escape(type.toString())) + "," //
                    + Util.quoted(escape(title)) + "," //
                    + Util.quoted(hex) + "," //
                    + Util.quoted(filename);
        }
    }

    private static String removeQuotes(String s) {
        return s.trim().substring(1, s.length() - 1);
    }

}
