package au.gov.amsa.sgb.decoder.internal.json;

final class MyField {
    private final String name;
    private final String javaType;
    private final boolean required;

    MyField(String name, String javaType, boolean required) {
        this.name = name;
        this.javaType = javaType;
        this.required = required;
    }

    String name() {
        return name;
    }

    String javaType() {
        return javaType;
    }

    boolean isRequired() {
        return required;
    }
}