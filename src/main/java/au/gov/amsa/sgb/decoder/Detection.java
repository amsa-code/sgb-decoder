package au.gov.amsa.sgb.decoder;

import java.math.BigDecimal;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

import au.gov.amsa.sgb.decoder.internal.Bits;
import au.gov.amsa.sgb.decoder.internal.Hex;
import au.gov.amsa.sgb.decoder.internal.json.Json;
import au.gov.amsa.sgb.decoder.rotatingfield.ActivationMethod;
import au.gov.amsa.sgb.decoder.rotatingfield.BeaconFeedback;
import au.gov.amsa.sgb.decoder.rotatingfield.Cancellation;
import au.gov.amsa.sgb.decoder.rotatingfield.DeactivationMethod;
import au.gov.amsa.sgb.decoder.rotatingfield.EltDtInFlightEmergency;
import au.gov.amsa.sgb.decoder.rotatingfield.GnssStatus;
import au.gov.amsa.sgb.decoder.rotatingfield.NationalUse;
import au.gov.amsa.sgb.decoder.rotatingfield.ObjectiveRequirements;
import au.gov.amsa.sgb.decoder.rotatingfield.Range;
import au.gov.amsa.sgb.decoder.rotatingfield.Rls;
import au.gov.amsa.sgb.decoder.rotatingfield.RlsProvider;
import au.gov.amsa.sgb.decoder.rotatingfield.RlsType;
import au.gov.amsa.sgb.decoder.rotatingfield.RotatingField;
import au.gov.amsa.sgb.decoder.rotatingfield.TriggeringEvent;
import au.gov.amsa.sgb.decoder.rotatingfield.UnknownRotatingField;
import au.gov.amsa.sgb.decoder.vesselid.AircraftOperatorAndSerialNumber;
import au.gov.amsa.sgb.decoder.vesselid.AircraftRegistrationMarking;
import au.gov.amsa.sgb.decoder.vesselid.Aviation24BitAddress;
import au.gov.amsa.sgb.decoder.vesselid.Mmsi;
import au.gov.amsa.sgb.decoder.vesselid.RadioCallSign;
import au.gov.amsa.sgb.decoder.vesselid.VesselId;

/**
 * Decodes a 202 bit binary beacon detection message. Based on C/T.018 Rev 6
 * (May 2020).
 */
public final class Detection {

    private static final Bits NO_ENCODED_LOCATION_CAPABILITY = Bits
            .from("11111111000001111100000111111111111110000011111");

    private static final Bits BCH_DIVISOR = Bits.from("1110001111110101110000101110111110011110010010111");

    @JsonIgnore
    private final Bits bits;

    private final int tac;
    private final int serialNo;
    private final int countryCode;
    private final boolean hasAtLeastOneEnabledHomingSignal;
    private final boolean hasEnabledRls;
    private final boolean testProtocolMessage;
    private final Optional<EncodedGnssPosition> encodedGnssPosition;
    private final Optional<VesselId> vesselId;
    private final BeaconType beaconType;
    private final RotatingField rotatingField;
    private final String beacon23HexId;
    private final String beacon15HexId;

    private Detection(Bits bits) {
        Preconditions.checkArgument(bits.length() == 202, "length should be 202 but was " + bits.length());
        this.bits = bits;
        tac = bits.readUnsignedInt(16);
        serialNo = bits.readUnsignedInt(14);
        countryCode = bits.readUnsignedInt(10);
        hasAtLeastOneEnabledHomingSignal = bits.readBoolean();
        hasEnabledRls = bits.readBoolean();
        testProtocolMessage = bits.readBoolean();
        encodedGnssPosition = readPosition(bits.readBits(47));
        vesselId = readVesselId(bits);
        beaconType = readBeaconType(bits);
        // skip spare bits
        bits.skip(14);
        rotatingField = readRotatingField(bits);
        beacon23HexId = readBeacon23HexID(bits.position(0));
        beacon15HexId = beacon23HexId.substring(0, 15);
    }

    public static Detection fromBitString(String bitString) {
        return from(Bits.from(bitString));
    }

    public static Detection fromHexGroundSegmentRepresentation(String hex) {
        String bitString = Hex.hexToBinary(hex).substring(2);
        return fromBitString(bitString);
    }

    public static Detection from(Bits bits) {
        return new Detection(bits);
    }

    private String readBeacon23HexID(Bits bits) {
        StringBuilder s = new StringBuilder();
        s.append("1");
        bits.position(0);
        String tacBits = bits.readBitString(16);
        String serialBits = bits.readBitString(14);
        String countryCodeBits = bits.readBitString(10);
        s.append(countryCodeBits);
        s.append("101");
        s.append(tacBits);
        s.append(serialBits);
        String testProtocolFlagBits = bits.position(42).readBitString(1);
        s.append(testProtocolFlagBits);
        String vesselIdBits = bits.position(90).readBitString(47);
        s.append(vesselIdBits);
        Bits b = Bits.from(s.toString());
        return b.readHex(23);
    }

    @VisibleForTesting
    static RotatingField readRotatingField(Bits bits) {
        // read rotating field identifier
        int rid = bits.readUnsignedInt(4);
        if (rid == 0) {
            return readRotatingFieldObjectiveRequirements(bits);
        } else if (rid == 1) {
            return readRotatingFieldEltDtInFlightEmergency(bits);
        } else if (rid == 2) {
            return readRotatingFieldRls(bits);
        } else if (rid == 3) {
            return readRotatingFieldNationalUse(bits);
        } else if (rid == 15) {
            return readRotatingFieldCancellationMessage(bits);
        } else {
            return readRotatingFieldUnknown(bits);
        }
    }

    private static ObjectiveRequirements readRotatingFieldObjectiveRequirements(Bits bits) {
        int elapsedTimeSinceActivationHours = bits.readUnsignedInt(6);
        int timeSinceLastEncodedLocationMinutes = bits.readUnsignedInt(11);
        int altitudeEncodedLocationMetres = readAltitudeEncodedLocationMetres(bits);
        Optional<Range> dilutionPrecisionHdop = readDop(bits);
        Optional<Range> dilutionPrecisionDop = readDop(bits);
        ActivationMethod activationMethod = readActivationMethod(bits);
        Optional<Range> remainingBatteryCapacityPercent = readBatteryPercent(bits);
        GnssStatus gnssStatus = readGnssStatus(bits);
        bits.skip(2);
        return new ObjectiveRequirements(elapsedTimeSinceActivationHours, timeSinceLastEncodedLocationMinutes,
                altitudeEncodedLocationMetres, dilutionPrecisionHdop, dilutionPrecisionDop, activationMethod,
                remainingBatteryCapacityPercent, gnssStatus);
    }

    @VisibleForTesting
    static GnssStatus readGnssStatus(Bits bits) {
        return toGnssStatus(bits.readUnsignedInt(2));
    }

    private static GnssStatus toGnssStatus(int code) {
        if (code == 0) {
            return GnssStatus.NO_FIX;
        } else if (code == 1) {
            return GnssStatus.LOCATION_2D;
        } else if (code == 2) {
            return GnssStatus.LOCATION_3D;
        } else {
            return GnssStatus.OTHER;
        }
    }

    @VisibleForTesting
    static Optional<Range> readBatteryPercent(Bits bits) {
        return toBatteryPercent(bits.readUnsignedInt(3));
    }

    private static Optional<Range> toBatteryPercent(int code) {
        if (code == 0) {
            return Optional.of(Range.min(0).max(5).build());
        } else if (code == 1) {
            return Optional.of(Range.min(5).exclusive().max(10).build());
        } else if (code == 2) {
            return Optional.of(Range.min(10).exclusive().max(25).build());
        } else if (code == 3) {
            return Optional.of(Range.min(25).exclusive().max(50).build());
        } else if (code == 4) {
            return Optional.of(Range.min(50).exclusive().max(75).build());
        } else if (code == 5) {
            return Optional.of(Range.min(75).exclusive().max(100).build());
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static Optional<Range> readDop(Bits bits) {
        return toDop(bits.readUnsignedInt(4));
    }

    private static Optional<Range> toDop(int code) {
        if (code == 0) {
            return Optional.of(Range.min(0).max(1).build());
        } else if (code <= 7) {
            return Optional.of(Range.min(code).exclusive().max(code + 1).build());
        } else if (code == 8) {
            return Optional.of(Range.min(8).exclusive().max(10).build());
        } else if (code == 9) {
            return Optional.of(Range.min(10).exclusive().max(12).build());
        } else if (code == 10) {
            return Optional.of(Range.min(12).exclusive().max(15).build());
        } else if (code == 11) {
            return Optional.of(Range.min(15).exclusive().max(20).build());
        } else if (code == 12) {
            return Optional.of(Range.min(20).exclusive().max(30).build());
        } else if (code == 13) {
            return Optional.of(Range.min(30).exclusive().max(50).build());
        } else if (code == 14) {
            return Optional.of(Range.min(50).exclusive().build());
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static ActivationMethod readActivationMethod(Bits bits) {
        return toActivationMethod(bits.readUnsignedInt(2));
    }

    private static ActivationMethod toActivationMethod(int code) {
        if (code == 0) {
            return ActivationMethod.MANUAL_ACTIVATION_BY_USER;
        } else if (code == 1) {
            return ActivationMethod.AUTOMATIC_ACTIVATION_BY_BEACON;
        } else if (code == 2) {
            return ActivationMethod.AUTOMATIC_ACTIVATION_BY_EXTERNAL_MEANS;
        } else {
            return ActivationMethod.OTHER;
        }
    }

    private static EltDtInFlightEmergency readRotatingFieldEltDtInFlightEmergency(Bits bits) {
        OffsetTime timeOfLastEncodedLocationSeconds = readTimeOfLastEncodedLocationSeconds(bits);
        int altitudeEncodedLocationMetres = readAltitudeEncodedLocationMetres(bits);
        TriggeringEvent triggeringEvent = readTriggeringEvent(bits);
        GnssStatus gnssStatus = readGnssStatus(bits);
        Optional<Range> remainingBatteryCapacityPercent = readBatteryPercentInFlightEmergency(bits);
        bits.skip(9);
        return new EltDtInFlightEmergency(timeOfLastEncodedLocationSeconds, altitudeEncodedLocationMetres,
                triggeringEvent, gnssStatus, remainingBatteryCapacityPercent);
    }

    private static int readAltitudeEncodedLocationMetres(Bits bits) {
        return bits.readUnsignedInt(10) * 16 - 400;
    }

    @VisibleForTesting
    static OffsetTime readTimeOfLastEncodedLocationSeconds(Bits bits) {
        int seconds = bits.readUnsignedInt(17);
        return OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC).plus(seconds, ChronoUnit.SECONDS);
    }

    @VisibleForTesting
    static Optional<Range> readBatteryPercentInFlightEmergency(Bits bits) {
        return toBatteryPercentInFlightEmergency(bits.readUnsignedInt(2));
    }

    private static Optional<Range> toBatteryPercentInFlightEmergency(int code) {
        if (code == 0) {
            return Optional.of(Range.min(0).max(33).build());
        } else if (code == 1) {
            return Optional.of(Range.min(33).exclusive().max(66).build());
        } else if (code == 2) {
            return Optional.of(Range.min(66).exclusive().max(100).build());
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static TriggeringEvent readTriggeringEvent(Bits bits) {
        return toTriggeringEvent(bits.readUnsignedInt(4));
    }

    private static TriggeringEvent toTriggeringEvent(int code) {
        if (code == 1) {
            return TriggeringEvent.MANUAL_ACTIVATION_BY_CREW;
        } else if (code == 4) {
            return TriggeringEvent.G_SWITCH_OR_DEFORMATION_ACTIVATION;
        } else if (code == 8) {
            return TriggeringEvent.AUTOMATIC_ACTIVATION_FROM_AVIONICS_OR_TRIGGERING_SYSTEM;
        } else {
            return TriggeringEvent.OTHER;
        }
    }

    private static Rls readRotatingFieldRls(Bits bits) {
        bits.skip(2);
        boolean canProcessAutomaticallyGeneratedAckRlmType1 = bits.readBoolean();
        boolean canProcessManuallyGeneratedRlm = bits.readBoolean();
        bits.skip(4);
        RlsProvider rlsProvider = readRlsProvider(bits);
        Optional<BeaconFeedback> beaconFeedback = readBeaconFeadback(bits, rlsProvider);
        bits.skip(11);
        return new Rls(canProcessAutomaticallyGeneratedAckRlmType1, canProcessManuallyGeneratedRlm, rlsProvider,
                beaconFeedback);
    }

    @VisibleForTesting
    static RlsProvider readRlsProvider(Bits bits) {
        return toRlsProvider(bits.readUnsignedInt(3));
    }

    private static RlsProvider toRlsProvider(int code) {
        if (code == 1) {
            return RlsProvider.GALILEO;
        } else if (code == 2) {
            return RlsProvider.GLONASS;
        } else {
            return RlsProvider.OTHER;
        }
    }

    @VisibleForTesting
    static Optional<BeaconFeedback> readBeaconFeadback(Bits bits, RlsProvider rlsProvider) {
        boolean rlmType1FeedbackReceived = bits.readBoolean();
        boolean rlmType2FeedbackReceived = bits.readBoolean();
        RlsType rlsType = readRlsType(bits);
        if (rlsProvider == RlsProvider.GALILEO) {
            final Optional<String> shortRlmParametersBitString;
            if (rlsType == RlsType.ACKNOWLEDGEMENT_SERVICE) {
                bits.skip(2);
                shortRlmParametersBitString = Optional.of(bits.readBitString(13));
                // skip parity bit
                bits.skip(1);
            } else if (rlsType == RlsType.TEST_SERVICE) {
                shortRlmParametersBitString = Optional.of(bits.readBitString(15));
                // skip parity bit
                bits.skip(1);
            } else {// OTHER
                shortRlmParametersBitString = Optional.of(bits.readBitString(15));
            }
            return Optional.of(new BeaconFeedback(rlmType1FeedbackReceived, rlmType2FeedbackReceived, rlsType,
                    shortRlmParametersBitString));
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static RlsType readRlsType(Bits bits) {
        return toRlsType(bits.readUnsignedInt(4));
    }

    private static RlsType toRlsType(int code) {
        if (code == 1) {
            return RlsType.ACKNOWLEDGEMENT_SERVICE;
        } else if (code == 15) {
            return RlsType.TEST_SERVICE;
        } else {
            return RlsType.OTHER;
        }
    }

    private static NationalUse readRotatingFieldNationalUse(Bits bits) {
        String bitString = bits.readBitString(44);
        return new NationalUse(bitString);
    }

    @VisibleForTesting
    static Cancellation readRotatingFieldCancellationMessage(Bits bits) {
        bits.skip(42);
        DeactivationMethod deactivationMethod = toDeactivationMethod(bits.readUnsignedInt(2));
        return new Cancellation(deactivationMethod);
    }

    private static DeactivationMethod toDeactivationMethod(int code) {
        if (code == 1) {
            return DeactivationMethod.AUTOMATIC_DEACTIVATION_BY_EXTERNAL_MEANS;
        } else if (code == 2) {
            return DeactivationMethod.MANUAL_DEACTIVATION_BY_USER;
        } else {
            return DeactivationMethod.OTHER;
        }
    }

    private static RotatingField readRotatingFieldUnknown(Bits bits) {
        String bitString = bits.readBitString(44);
        return new UnknownRotatingField(bitString);
    }

    @VisibleForTesting
    static Optional<EncodedGnssPosition> readPosition(Bits bits) {
        Preconditions.checkArgument(bits.length() == 47);
        if (bits.isEqualTo(NO_ENCODED_LOCATION_CAPABILITY)) {
            return Optional.empty();
        } else {
            double lat = readLat(bits);
            double lon = readLon(bits);
            return Optional.of(new EncodedGnssPosition(lat, lon));
        }
    }

    private static double readLat(Bits bits) {
        return readPositionCoordinate(bits, 7);
    }

    private static double readLon(Bits bits) {
        return readPositionCoordinate(bits, 8);
    }

    private static double readPositionCoordinate(Bits bits, int numDegreesBits) {
        int sign = bits.readBoolean() ? -1 : 1;
        int degrees = bits.readUnsignedInt(numDegreesBits);
        // use BigDecimal so don't lose precision
        BigDecimal d = readDecimalPart(bits);
        d = d.add(BigDecimal.valueOf(degrees));
        d = d.multiply(BigDecimal.valueOf(sign));
        return d.doubleValue();
    }

    private static BigDecimal readDecimalPart(Bits bits) {
        boolean[] decimalPart = bits.readBooleanArray(15);
        BigDecimal d = BigDecimal.ZERO;
        BigDecimal f = BigDecimal.ONE;
        for (int i = 0; i < decimalPart.length; i++) {
            f = f.divide(BigDecimal.valueOf(2));
            if (decimalPart[i]) {
                d = d.add(f);
            }
        }
        return d;
    }

    @VisibleForTesting
    static Optional<VesselId> readVesselId(Bits bits) {
        int type = bits.readUnsignedInt(3);
        if (type == 0) {
            // no aircraft or maritime identity
            bits.skip(44);
            return Optional.empty();
        } else if (type == 1) {
            return Optional.of(readVesselIdMmsi(bits));
        } else if (type == 2) {
            return Optional.of(readVesselIdRadioCallSign(bits));
        } else if (type == 3) {
            return Optional.of(readVesselIdAicraftRegistrationMarking(bits));
        } else if (type == 4) {
            return Optional.of(readVesselIdAviation24BitAddress(bits));
        } else if (type == 5) {
            return Optional.of(readVesselIdAircraftOperatoAndSerialNumber(bits));
        } else {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    static AircraftOperatorAndSerialNumber readVesselIdAircraftOperatoAndSerialNumber(Bits bits) {
        String aircraftOperatorDesignator = bits.readBaudotCharactersShort(3);
        int serialNumber = bits.readUnsignedInt(12);
        bits.skip(17);
        return new AircraftOperatorAndSerialNumber(aircraftOperatorDesignator, serialNumber);
    }

    @VisibleForTesting
    static Mmsi readVesselIdMmsi(Bits bits) {
        int mmsi = bits.readUnsignedInt(30);
        String mmsiString = padLeftWithZeros(mmsi, 9);
        int epirbLast4 = bits.readUnsignedInt(14);
        String epirbLast4String = padLeftWithZeros(epirbLast4, 4);
        String epirbMmsi = "974" + mmsiString.charAt(3) + mmsiString.charAt(4) + epirbLast4String;
        final Optional<Integer> a;
        if (mmsi == 111111) {
            a = Optional.empty();
        } else {
            a = Optional.of(mmsi);
        }
        final Optional<Integer> b;
        if (epirbLast4 == 10922) {
            b = Optional.empty();
        } else {
            b = Optional.of(Integer.parseInt(epirbMmsi));
        }
        Mmsi m = new Mmsi(a, b);
        return m;
    }

    @VisibleForTesting
    static String padLeftWithZeros(long number, int desiredLength) {
        String s = Long.toString(number);
        while (s.length() < desiredLength) {
            s = "0" + s;
        }
        return s;
    }

    @VisibleForTesting
    static RadioCallSign readVesselIdRadioCallSign(Bits bits) {
        bits.skip(2);
        String s = bits.readBaudotCharacters(7).trim();
        return new RadioCallSign(s);
    }

    @VisibleForTesting
    static AircraftRegistrationMarking readVesselIdAicraftRegistrationMarking(Bits bits) {
        bits.skip(2);
        String s = bits.readBaudotCharacters(7).trim();
        return new AircraftRegistrationMarking(s);
    }

    @VisibleForTesting
    static Aviation24BitAddress readVesselIdAviation24BitAddress(Bits bits) {
        String addressHex = bits.readHex(6);
        final Optional<String> aircraftOperatorDesignator;
        Bits b = bits.readBits(20);
        if (b.isZero()) {
            aircraftOperatorDesignator = Optional.empty();
        } else {
            aircraftOperatorDesignator = Optional.of(b.readBaudotCharactersShort(3));
        }
        return new Aviation24BitAddress(addressHex, aircraftOperatorDesignator);
    }

    @VisibleForTesting
    static BeaconType readBeaconType(Bits bits) {
        int v = bits.readUnsignedInt(3);
        if (v == 0) {
            return BeaconType.ELT_NOT_DT;
        } else if (v == 1) {
            return BeaconType.EPIRB;
        } else if (v == 2) {
            return BeaconType.PLB;
        } else if (v == 3) {
            return BeaconType.ELT_DT;
        } else if (v == 7) {
            return BeaconType.SYSTEM;
        } else {
            return BeaconType.OTHER;
        }
    }

    public int tac() {
        return tac;
    }

    public int serialNo() {
        return serialNo;
    }

    public int countryCode() {
        return countryCode;
    }

    public boolean hasAtLeastOneEnabledHomingSignal() {
        return hasAtLeastOneEnabledHomingSignal;
    }

    public boolean hasEnabledRls() {
        return hasEnabledRls;
    }

    public boolean isTestProtocolMessage() {
        return testProtocolMessage;
    }

    public Optional<EncodedGnssPosition> encodedGnssPosition() {
        return encodedGnssPosition;
    }

    public Optional<? extends VesselId> vesselId() {
        return vesselId;
    }

    public BeaconType beaconType() {
        return beaconType;
    }

    public RotatingField rotatingField() {
        return rotatingField;
    }

    public String beacon23HexId() {
        return beacon23HexId;
    }

    public String beacon15HexId() {
        return beacon15HexId;
    }

    public Bits calculateBchErrorCorrectionCode() {
        return bits //
                .concatWith(Bits.zeros(48)) //
                .remainderOnPolynomialDivision(BCH_DIVISOR) //
                .last(48);
    }

    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

}
