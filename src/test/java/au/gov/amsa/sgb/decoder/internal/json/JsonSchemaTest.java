package au.gov.amsa.sgb.decoder.internal.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.junit.Asserts;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import au.gov.amsa.sgb.decoder.Beacon23HexId;
import au.gov.amsa.sgb.decoder.Detection;
import au.gov.amsa.sgb.decoder.TestingUtil;
import au.gov.amsa.sgb.decoder.rotatingfield.Cancellation;
import au.gov.amsa.sgb.decoder.rotatingfield.EltDtInFlightEmergency;
import au.gov.amsa.sgb.decoder.rotatingfield.NationalUse;
import au.gov.amsa.sgb.decoder.rotatingfield.ObjectiveRequirements;
import au.gov.amsa.sgb.decoder.rotatingfield.Rls;
import au.gov.amsa.sgb.decoder.rotatingfield.RotatingField;
import au.gov.amsa.sgb.decoder.rotatingfield.UnknownRotatingField;
import au.gov.amsa.sgb.decoder.vesselid.AircraftOperatorAndSerialNumber;
import au.gov.amsa.sgb.decoder.vesselid.AircraftRegistrationMarking;
import au.gov.amsa.sgb.decoder.vesselid.Aviation24BitAddress;
import au.gov.amsa.sgb.decoder.vesselid.Mmsi;
import au.gov.amsa.sgb.decoder.vesselid.RadioCallSign;
import au.gov.amsa.sgb.decoder.vesselid.VesselId;

public class JsonSchemaTest {

    private static final String SCHEMA_ID = "https://amsa.gov.au/sgb";

    @Test
    public void updateBeacon23HexIdSchemaInSourceAndEnsureExampleJsonCompliesWithSchema()
            throws IOException, ProcessingException {
        String schema = generateSchemaFromBeacon23HexIdClass();
        File file = new File("src/main/resources/beacon-23-hex-id-schema.json");
        file.delete();
        Files.write(file.toPath(), schema.getBytes(StandardCharsets.UTF_8));

        // check detection.json is valid with schema
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        ObjectMapper m = new ObjectMapper();
        com.github.fge.jsonschema.main.JsonSchema jsonSchema = factory.getJsonSchema(m.readTree(file));
        String example = TestingUtil.readResource("/compliance-kit/beacon-23-hex-id-sample.json");
        JsonNode json = m.readTree(example);
        ProcessingReport report = jsonSchema.validate(json);
        // System.out.println(report);
        assertTrue(report.isSuccess());
    }

    @Test
    public void updateDetectionSchemaInSourceAndEnsureExampleJsonCompliesWithSchema()
            throws IOException, ProcessingException {
        String schema = generateSchemaFromDetectionClass();
        File file = new File("src/main/resources/detection-schema.json");
        file.delete();
        Files.write(file.toPath(), schema.getBytes(StandardCharsets.UTF_8));

        // check detection.json is valid with schema
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        ObjectMapper m = new ObjectMapper();
        com.github.fge.jsonschema.main.JsonSchema jsonSchema = factory.getJsonSchema(m.readTree(file));
        String example = TestingUtil.readResource("/compliance-kit/detection-specification-example.json");
        JsonNode json = m.readTree(example);
        ProcessingReport report = jsonSchema.validate(json);
        // System.out.println(report);
        assertTrue(report.isSuccess());
    }

    @Test
    public void test() throws JsonProcessingException, ProcessingException, IOException {
        File file = new File("src/main/resources/detection-schema.json");
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        ObjectMapper m = new ObjectMapper();
        com.github.fge.jsonschema.main.JsonSchema jsonSchema = factory.getJsonSchema(m.readTree(file));
        JsonNode json = m.readTree("{\"thing\" : 123}");
        ProcessingReport report = jsonSchema.validate(json);
        assertFalse(report.isSuccess());
    }

    private static String generateSchemaFromDetectionClass() {
        Map<Class<?>, List<Class<?>>> map = new HashMap<>();
        map.put(VesselId.class, Arrays.asList(AircraftOperatorAndSerialNumber.class, AircraftRegistrationMarking.class,
                Aviation24BitAddress.class, Mmsi.class, RadioCallSign.class));
        map.put(RotatingField.class, Arrays.asList(Cancellation.class, EltDtInFlightEmergency.class, NationalUse.class,
                ObjectiveRequirements.class, Rls.class, UnknownRotatingField.class));
        return Json.prettyPrint(JsonSchema.generateSchema(Detection.class, map, SCHEMA_ID));
    }

    private static String generateSchemaFromBeacon23HexIdClass() {
        Map<Class<?>, List<Class<?>>> map = new HashMap<>();
        map.put(VesselId.class, Arrays.asList(AircraftOperatorAndSerialNumber.class, AircraftRegistrationMarking.class,
                Aviation24BitAddress.class, Mmsi.class, RadioCallSign.class));
        return Json.prettyPrint(JsonSchema.generateSchema(Beacon23HexId.class, map, SCHEMA_ID));
    }

    @Test
    public void testSimpleNameNoPackage() {
        assertEquals("Hello", JsonSchema.simpleName("Hello"));
    }

    @Test
    public void testSimpleNameHasPackage() {
        assertEquals("There", JsonSchema.simpleName("hello.There"));
    }

    @Test(expected = RuntimeException.class)
    public void testToClassNotFound() {
        JsonSchema.toClass("ThisClassDoesNotExist");
    }

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(JsonSchema.class);
    }

    @Test
    public void testRecursiveSchemaDoesNotOverflowStack() {
        JsonSchema.generateSchema(Recursive.class, new HashMap<>(), SCHEMA_ID);
    }

    @Test
    public void testSchemaFromPrimitive() {
        assertTrue(
                JsonSchema.generateSchema(Integer.class, new HashMap<>(), SCHEMA_ID).contains("\"definitions\" : {}"));
    }

    private static final class Recursive {
        @SuppressWarnings("unused")
        int number;

        @SuppressWarnings("unused")
        Recursive rec;
    }
}
