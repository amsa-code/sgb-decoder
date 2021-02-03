package au.gov.amsa.sgb.decoder.internal.json;

final class MyField {
    final String name;
    final String javaType;
    final boolean required;

    MyField(String name, String javaType, boolean required) {
        this.name = name;
        this.javaType = javaType;
        this.required = required;
    }
}