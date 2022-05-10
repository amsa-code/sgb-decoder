package au.gov.amsa.sgb.decoder.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

/**
 * See C/S T.021 specification document.
 *
 */
public final class Tac {

    @VisibleForTesting
    static final BinaryOperator<String> MERGE_FUNCTION = (v1, v2) -> {
        throw new IllegalArgumentException(String.format("Duplicate key for values %s and %s", v1, v2));
    };

    // Lazy load to avoid static initializer running when calling loadDescriptions
    // in a test
    private TreeMap<Integer, String> descriptions;

    private static final Tac INSTANCE = new Tac();

    private Tac() {
        descriptions = loadDescriptions(() -> Tac.class.getResourceAsStream("/tac-descriptions.txt"));
    }

    public static Optional<String> description(int tac) {
        return Optional.ofNullable(INSTANCE.descriptions.floorEntry(tac)).map(x -> x.getValue());
    }

    @VisibleForTesting
    static TreeMap<Integer, String> loadDescriptions(Supplier<InputStream> in) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in.get(), StandardCharsets.UTF_8))) {
            return br //
                    .lines() //
                    .map(line -> line.trim()) //
                    .filter(line -> !line.startsWith("#")) //
                    .filter(line -> !line.isEmpty()) //
                    .map(line -> line.split(",")) //
                    .collect(Collectors.toMap(items -> Integer.parseInt(items[0]), //
                            items -> items[1], //
                            MERGE_FUNCTION, //
                            TreeMap::new));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
