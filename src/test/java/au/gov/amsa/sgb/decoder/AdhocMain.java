package au.gov.amsa.sgb.decoder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.io.Files;

public class AdhocMain {

    public static void main(String[] args) throws IOException {
        List<String> list = Files.readLines(new File("/home/dxm/temp.txt"), StandardCharsets.UTF_8);
        list.stream() //
                .map(x -> x.trim()) //
                .filter(x -> !x.isEmpty()) //
                .forEach(x -> {
                    try {
                        System.out.println("--------------");
                        System.out.println(x);
                        System.out.println(Beacon23HexId.fromHex(x).toJson());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

}
