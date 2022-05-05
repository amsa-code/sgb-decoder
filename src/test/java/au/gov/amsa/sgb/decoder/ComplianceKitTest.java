package au.gov.amsa.sgb.decoder;

import static au.gov.amsa.sgb.decoder.TestingUtil.assertJsonEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        // When new hex tests are obtained (confirmed ones from community/spec authors)
        // then they are added below. Once happy with the tests they are copied manually
        // from
        // target/compliance-kit to src/test/resources/compliance-kit as a permanent
        // test for the build

        File kit = new File("target/compliance-kit");
        kit.mkdirs();
        Arrays.stream(kit.listFiles()).forEach(File::delete);

        List<KitTest> tests = new ArrayList<>();
        KitTest.type(TestType.DETECTION) //
                .title("Specification example B-1 no Vessel Id") //
                .hex(DetectionTest.SAMPLE_HEX) //
                .filename("detection-specification-example.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.DETECTION) //
                .title("Example B-2 with Vessel Id of type MMSI") //
                .hex(DetectionTest.createBitsWithVesselIdFieldMmsi()) //
                .filename("detection-with-mmsi-vessel-id.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.DETECTION) //
                .title("Example B-2 with Vessel Id Aircraft Operator and Serial Number") //
                .hex(DetectionTest.createBitsWithVesselIdFieldAircraftOperatorAndSerialNumber()) //
                .filename("detection-with-aircraft-operator-and-serial-number.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.DETECTION) //
                .title("Example B-2 with Vessel Id Aircraft Registration Marking") //
                .hex(DetectionTest.createBitsWithVesselIdFieldAircraftRegistrationMarkingVhAbc()) //
                .filename("detection-with-aircraft-registration-marking.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 minimal") //
                .hex("9977DA6D709000000000000") //
                .filename("beacon-23-hex-id-minimal.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 with mmsi") //
                .hex("ADF587AA62B157AE36DC552") //
                .filename("beacon-23-hex-id-with-mmsi.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 with aircraft reg") //
                .hex("A2F669AB2D930E18709B40C") //
                .filename("beacon-23-hex-id-with-aircraft-reg.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 with aviation 24 bit address") //
                .hex("ADF68E50F4B47C5D5700000") //
                .filename("beacon-23-hex-id-with-aviation-24-bit-address.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 plb with aviation 24 bit address") //
                .hex("99B49C6B4E9444914B00000") //
                .filename("beacon-23-hex-id-plb-with-aviation-24-bit-address.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 plb with radio call sign") //
                .hex("A794B4C00872E33B9D64A04") //
                .filename("beacon-23-hex-id-plb-with-radio-call-sign.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 plb with aircraft operator and serial number") //
                .hex("9A749FFF0395CB3D62BBFFF") //
                .filename("beacon-23-hex-id-plb-with-aircraft-operator.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 epirb with radio call sign") //
                .hex("A794CEE960B2C2C54DA6AB8") //
                .filename("beacon-23-hex-id-epirb-with-radio-call-sign.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 with radio call sign") //
                .hex("9C55E7DC0172F33D7E75974") //
                .filename("beacon-23-hex-id-with-radio-call-sign.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 epirb with aircraft reg") //
                .hex("CF54D901AF93FA6EAD72B90") //
                .filename("beacon-23-hex-id-epirb-with-aircraft-reg.json") //
                .addTo(kit, tests);
        KitTest.type(TestType.BEACON_23_HEX_ID) //
                .title("G.005 epirb") //
                .hex("BB34CB138F5000000000000") //
                .filename("beacon-23-hex-id-epirb.json") //
                .addTo(kit, tests);

        writeCsv(kit, tests);
        runAllComplianceTestsInFolder(kit);
    }

    private static void writeCsv(File kit, List<KitTest> tests) {
        StringBuilder b = new StringBuilder();
        b.append("TYPE,TITLE,HEX,JSON\n");
        for (KitTest test : tests) {
            b.append(test.toCsvLine());
            b.append("\n");
        }
        try {
            Files.write(b.toString().getBytes(StandardCharsets.UTF_8), new File(kit, "tests.csv"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    private static void runAllComplianceTestsInFolder(File base) throws IOException, ComparisonFailure {
        Charset charset = StandardCharsets.UTF_8;
        File file = new File(base, "tests.csv");
        List<String> lines = Files.readLines(file, charset);
        lines.stream() //
                .filter(line -> !line.startsWith("#")) //
                .skip(1) //
                .map(line -> line.trim()) //
                .filter(line -> !line.isEmpty()) //
                .forEach(line -> {
                    String[] items = line.split(",");
                    assertEquals(4, items.length);
                    String type = removeQuotes(items[0]);
                    String hex = removeQuotes(items[2]);
                    String filename = removeQuotes(items[3]);
                    String json;
                    try {
                        json = Files.readLines(new File(base, filename), charset) //
                                .stream() //
                                .collect(Collectors.joining("\n"));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (type.trim().equalsIgnoreCase("Detection")) {
                        Detection d = Detection.fromHexGroundSegmentRepresentation(hex);
                        assertJsonEquals(json, d.toJson());
                    } else {
                        Beacon23HexId b = Beacon23HexId.fromHex(hex);
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
