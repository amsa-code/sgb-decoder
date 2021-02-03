package au.gov.amsa.sgb.decoder.rotatingfield;

public final class UnknownRotatingField implements RotatingField {

    // used for serialization
    private final RotatingFieldType rotatingFieldType = RotatingFieldType.UNKNOWN;
    private final String bitString;

    public UnknownRotatingField(String bitString) {
        this.bitString = bitString;
    }

    public RotatingFieldType rotatingFieldType() {
        return rotatingFieldType;
    }

    public String bitString() {
        return bitString;
    }

}
