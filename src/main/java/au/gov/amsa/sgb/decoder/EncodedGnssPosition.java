package au.gov.amsa.sgb.decoder;

public final class EncodedGnssPosition {

    private final double lat;
    private final double lon;

    public EncodedGnssPosition(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

}