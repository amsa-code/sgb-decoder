package au.gov.amsa.sgb.decoder.rotatingfield;

public final class Cancellation implements RotatingField {

    private final RotatingFieldType rotatingFieldType = RotatingFieldType.CANCELLATION;
    private final DeactivationMethod deactivationMethod;

    public Cancellation(DeactivationMethod deactivationMethod) {
        this.deactivationMethod = deactivationMethod;
    }

    public RotatingFieldType rotatingFieldType() {
        return rotatingFieldType;
    }

    public DeactivationMethod deactivationMethod() {
        return deactivationMethod;
    }

}
