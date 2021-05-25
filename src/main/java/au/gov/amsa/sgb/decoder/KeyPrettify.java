package au.gov.amsa.sgb.decoder;

public final class KeyPrettify {

    private KeyPrettify() {
        // prevent instantiation
    }

    public static String prettify(String key) {
        return replaceTokens(separateWords(key));
    }

    private static String separateWords(String key) {
        StringBuilder s = new StringBuilder();
        s.append(Character.toUpperCase(key.charAt(0)));
        boolean previousIsDigit = Character.isDigit(key.charAt(0));
        for (int i = 1; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (Character.isUpperCase(ch) || Character.isDigit(ch) && !previousIsDigit) {
                s.append(" ");
            }
            s.append(ch);
            previousIsDigit = Character.isDigit(ch);
        }
        return s.toString();
    }

    private static String replaceTokens(String words) {
        return words //
                .replaceAll("\\bRls\\b", "RLS")//
                .replaceAll("\\bRlm\\b", "RLM") //
                .replaceAll("\\bGnss\\b", "GNSS") //
                .replaceAll("\\bHdop\\b", "HDOP") //
                .replaceAll("\\bVdop\\b", "VDOP") //
                .replaceAll("\\bElt\\b", "ELT") //
                .replaceAll("\\bDt\\b", "DT");
    }
}
