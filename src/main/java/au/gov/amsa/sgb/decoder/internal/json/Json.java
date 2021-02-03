package au.gov.amsa.sgb.decoder.internal.json;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public final class Json {

    private static ObjectMapper MAPPER = createMapper();

    private Json() {
        // prevent instantiation
    }

    public static String toJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper createMapper() {
        ObjectMapper m = new ObjectMapper();

        // Avoid having to annotate the Person class
        // Requires Java 8, pass -parameters to javac
        // and jackson-module-parameter-names as a dependency
        return m//
                .registerModule(new ParameterNamesModule(PROPERTIES)) //
                .registerModule(new Jdk8Module().configureAbsentsAsNulls(true)) //
                .registerModule(new JavaTimeModule())
                // make private fields of visible to Jackson
                .setVisibility(FIELD, ANY) //
                .setSerializationInclusion(Include.NON_NULL);
    }

    public static boolean equals(String json1, String json2) {
        ObjectMapper m = new ObjectMapper();
        try {
            return m.readTree(json1).equals(m.readTree(json2));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrint(String json) {
        ObjectMapper m = new ObjectMapper();
        try {
            Object v = m.readValue(json, Object.class);
            return m.writerWithDefaultPrettyPrinter().writeValueAsString(v);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
