package au.gov.amsa.sgb.decoder.rotatingfield;

public final class NationalUse implements RotatingField {

    // used for serialization
    private final RotatingFieldType rotatingFieldType = RotatingFieldType.NATIONAL_USE;
    private final String bitString;

    public NationalUse(String bitString) {
        this.bitString = bitString;
    }

    @Override
    public RotatingFieldType rotatingFieldType() {
        return rotatingFieldType;
    }

    public String bitString() {
        return bitString;
    }

}
