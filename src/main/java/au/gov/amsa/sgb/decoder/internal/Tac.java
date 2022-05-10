package au.gov.amsa.sgb.decoder.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
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

    private static final TreeMap<Integer, String> DESCRIPTIONS = loadDescriptions();

    private Tac() {
        // prevent instantiation
    }

    public static Optional<String> description(int tac) {
        return Optional.ofNullable(DESCRIPTIONS.floorEntry(tac)).map(x -> x.getValue());
    }

    private static TreeMap<Integer, String> loadDescriptions() {
        return loadDescriptions(Tac.class.getResource("/tac-descriptions.txt"));
    }
    
    @VisibleForTesting
    static TreeMap<Integer, String> loadDescriptions(URL url) {
        try {
            // init FileSystem is necessary
            URI uri = url.toURI();
            FileSystem fs = initFileSystem(uri);
            List<String> list = Files.readAllLines(Paths.get(uri), StandardCharsets.UTF_8);
            fs.close();
            return list //
                    .stream() //
                    .map(line -> line.trim()) //
                    .filter(line -> !line.startsWith("#")) //
                    .filter(line -> !line.isEmpty()) //
                    .map(line -> line.split(",")) //
                    .collect(Collectors.toMap(items -> Integer.parseInt(items[0]), //
                            items -> items[1], //
                            MERGE_FUNCTION, //
                            TreeMap::new));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            return FileSystems.newFileSystem(uri, env);
        }
    }

}
