package au.gov.amsa.sgb.decoder;

import static au.gov.amsa.sgb.decoder.TestingUtil.ones;
import static au.gov.amsa.sgb.decoder.TestingUtil.zeros;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import org.junit.Test;

import au.gov.amsa.sgb.decoder.internal.Bits;
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
import au.gov.amsa.sgb.decoder.rotatingfield.TriggeringEvent;
import au.gov.amsa.sgb.decoder.rotatingfield.UnknownRotatingField;
import au.gov.amsa.sgb.decoder.vesselid.AircraftOperatorAndSerialNumber;
import au.gov.amsa.sgb.decoder.vesselid.AircraftRegistrationMarking;
import au.gov.amsa.sgb.decoder.vesselid.Aviation24BitAddress;
import au.gov.amsa.sgb.decoder.vesselid.Mmsi;
import au.gov.amsa.sgb.decoder.vesselid.RadioCallSign;
import au.gov.amsa.sgb.decoder.vesselid.VesselIdType;

public class DetectionTest {

    public static final String SAMPLE_HEX = "0039823D32618658622811F0000000000003FFF004030680258";
    public static final String BITS = "00000000111001100000100011110100" //
            + "11001001100001100001100101100001" //
            + "10001000101000000100011111000000" //
            + "00000000000000000000000000000000" //
            + "00000000000011111111111111000000" //
            + "00010000000011000001101000000000" //
            + "1001011000";

    @Test
    public void testFromHex() {
        Detection d = Detection.fromHexGroundSegmentRepresentation(SAMPLE_HEX);
        checkDetection(d);
    }

    @Test
    public void testToBits() {
        Detection d = Detection.fromBitString(BITS);
        checkDetection(d);
    }

    @Test
    public void testDetectionToString() {
        Detection d = Detection.fromBitString(BITS);
        assertEquals(d.toJson(), d.toString());
    }

    @Test
    public void testDetectionToJsonUsingJackson() throws IOException {
        Detection d = Detection.fromHexGroundSegmentRepresentation(SAMPLE_HEX);
        TestingUtil.assertResourceEqualsJson("/compliance-kit/detection-specification-example.json", d.toJson());
        File file = new File("src/docs/detection.json");
        Files.write(file.toPath(), Json.prettyPrint(d.toJson()).getBytes(StandardCharsets.UTF_8));
    }

    private void checkDetection(Detection d) {
        assertEquals(230, d.tac());
        assertEquals(573, d.serialNo());
        assertEquals(201, d.countryCode());
        assertTrue(d.hasAtLeastOneEnabledHomingSignal());
        assertFalse(d.hasEnabledRls());
        assertFalse(d.isTestProtocolMessage());
        EncodedGnssPosition p = d.encodedGnssPosition().get();
        assertEquals(48.793153539336956, p.lat(), 0.00001);
        assertEquals(69.00875866413116, p.lon(), 0.00001);
        assertFalse(d.vesselId().isPresent());
        assertEquals(BeaconType.ELT_NOT_DT, d.beaconType());
        ObjectiveRequirements r = (ObjectiveRequirements) d.rotatingField();
        assertEquals(1, r.elapsedTimeSinceActivationHours());
        assertEquals(6, r.timeSinceLastEncodedLocationMinutes());
        assertEquals(432, r.altitudeEncodedLocationMetres());
        assertEquals(Range.min(0).max(1).build(), r.dilutionPrecisionHdop().get());
        assertEquals(Range.min(1).exclusive().max(2).build(), r.dilutionPrecisionDop().get());
        assertEquals(ActivationMethod.MANUAL_ACTIVATION_BY_USER, r.activationMethod());
        assertEquals(Range.min(75).exclusive().max(100).build(), r.remainingBatteryCapacityPercent().get());
        assertEquals(GnssStatus.LOCATION_3D, r.gnssStatus());
        assertEquals("9934039823D000000000000", d.beacon23HexId());
        assertEquals("9934039823D0000", d.beacon15HexId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSize() {
        Detection.from(Bits.from("111"));
    }

    @Test
    public void testReadBeaconType() {
        assertEquals(BeaconType.ELT_NOT_DT, Detection.readBeaconType(Bits.from("000")));
        assertEquals(BeaconType.EPIRB, Detection.readBeaconType(Bits.from("001")));
        assertEquals(BeaconType.PLB, Detection.readBeaconType(Bits.from("010")));
        assertEquals(BeaconType.ELT_DT, Detection.readBeaconType(Bits.from("011")));
        assertEquals(BeaconType.SYSTEM, Detection.readBeaconType(Bits.from("111")));
        assertEquals(BeaconType.OTHER, Detection.readBeaconType(Bits.from("100")));
        assertEquals(BeaconType.OTHER, Detection.readBeaconType(Bits.from("101")));
        assertEquals(BeaconType.OTHER, Detection.readBeaconType(Bits.from("110")));
    }

    @Test
    public void testReadVesselIdAviation24BitAddressNoDesignator() {
        String address = "101011001000001011101100";
        Aviation24BitAddress a = Detection.readVesselIdAviation24BitAddress(Bits.from(address + zeros(20)));
        assertEquals("AC82EC", a.addressHex());
        assertFalse(a.aircraftOperatorDesignator().isPresent());
    }

    @Test
    public void testReadVesselIdAviation24BitAddressWithDesignator() {
        Aviation24BitAddress a = Detection.readVesselIdAviation24BitAddress(createVesselIdAviation24BitAddress());
        assertEquals("AC82EC", a.addressHex());
        assertEquals("ABC", a.aircraftOperatorDesignator().get());
    }

    @Test
    public void testReadVesselIdWithAviation24BitAddress() {
        Aviation24BitAddress a = (Aviation24BitAddress) Detection
                .readVesselId(Bits.from("100").concatWith(createVesselIdAviation24BitAddress())).get();
        assertEquals("AC82EC", a.addressHex());
    }

    static Bits createVesselIdAviation24BitAddress() {
        String address = "101011001000001011101100";
        String designator = "11000100110111000000";
        return Bits.from(address + designator);
    }

    @Test
    public void testReadVesselIdAicraftRegistrationMarking() {
        // two zeros then a space then VH-ABC using Baudot
        Bits b = createVesselIdAircraftRegistrationMarkingVhAbc();
        AircraftRegistrationMarking a = Detection.readVesselIdAicraftRegistrationMarking(b);
        assertEquals("VH-ABC", a.value().get());
    }

    static Bits createVesselIdAircraftRegistrationMarkingVhAbc() {
        return Bits.from("00100100101111100101011000111000110011101110");
    }

    @Test
    public void testReadVesselIdAicraftRegistrationMarkingNotPresent() {
        // two zeros then spaces using Baudot
        Bits b = Bits.from("00100100100100100100100100100100100100100100");
        AircraftRegistrationMarking a = Detection.readVesselIdAicraftRegistrationMarking(b);
        assertFalse(a.value().isPresent());
    }

    @Test
    public void testReadVesselIdWithAircraftRegistrationMarking() {
        AircraftRegistrationMarking a = (AircraftRegistrationMarking) Detection
                .readVesselId(Bits.from("011").concatWith(createVesselIdAircraftRegistrationMarkingVhAbc())).get();
        assertEquals("VH-ABC", a.value().get());
    }

    @Test
    public void testReadVesselIdRadioCallSign() {
        Bits b = createVesselIdRadioCallSignForBingo();
        RadioCallSign a = Detection.readVesselIdRadioCallSign(b);
        assertEquals("BINGO", a.value().get());
    }

    private static Bits createVesselIdRadioCallSignForBingo() {
        return Bits.from("00110011101100100110101011100011100100100100");
    }

    @Test
    public void testReadVesselIdRadioCallSignNotPresent() {
        Bits b = Bits.from("00100100100100100100100100100100100100100100");
        RadioCallSign a = Detection.readVesselIdRadioCallSign(b);
        assertFalse(a.value().isPresent());
    }

    @Test
    public void testReadVesselIdWithRadioCallSign() {
        RadioCallSign a = (RadioCallSign) Detection
                .readVesselId(Bits.from("010").concatWith(createVesselIdRadioCallSignForBingo())).get();
        assertEquals("BINGO", a.value().get());
    }

    @Test
    public void testPadLeft() {
        assertEquals("001", Detection.padLeftWithZeros(1, 3));
        assertEquals("012", Detection.padLeftWithZeros(12, 3));
        assertEquals("123", Detection.padLeftWithZeros(123, 3));
        assertEquals("1234", Detection.padLeftWithZeros(1234, 3));
    }

    @Test
    public void testGnssStatus() {
        assertEquals(GnssStatus.NO_FIX, Detection.readGnssStatus(Bits.from("00")));
        assertEquals(GnssStatus.LOCATION_2D, Detection.readGnssStatus(Bits.from("01")));
        assertEquals(GnssStatus.LOCATION_3D, Detection.readGnssStatus(Bits.from("10")));
        assertEquals(GnssStatus.OTHER, Detection.readGnssStatus(Bits.from("11")));
    }

    @Test
    public void testRemainingBattery() {
        assertEquals(Range.min(0).max(5).build(), Detection.readBatteryPercent(Bits.from("000")).get());
        assertEquals(Range.min(5).exclusive().max(10).build(), Detection.readBatteryPercent(Bits.from("001")).get());
        assertEquals(Range.min(10).exclusive().max(25).build(), Detection.readBatteryPercent(Bits.from("010")).get());
        assertEquals(Range.min(25).exclusive().max(50).build(), Detection.readBatteryPercent(Bits.from("011")).get());
        assertEquals(Range.min(50).exclusive().max(75).build(), Detection.readBatteryPercent(Bits.from("100")).get());
        assertEquals(Range.min(75).exclusive().max(100).build(), Detection.readBatteryPercent(Bits.from("101")).get());
        assertFalse(Detection.readBatteryPercent(Bits.from("110")).isPresent());
        assertFalse(Detection.readBatteryPercent(Bits.from("111")).isPresent());
    }

    @Test
    public void testDop() {
        assertEquals(Range.min(0).max(1).build(), Detection.readDop(Bits.from("0000")).get());
        assertEquals(Range.min(1).exclusive().max(2).build(), Detection.readDop(Bits.from("0001")).get());
        assertEquals(Range.min(2).exclusive().max(3).build(), Detection.readDop(Bits.from("0010")).get());
        assertEquals(Range.min(3).exclusive().max(4).build(), Detection.readDop(Bits.from("0011")).get());
        assertEquals(Range.min(4).exclusive().max(5).build(), Detection.readDop(Bits.from("0100")).get());
        assertEquals(Range.min(5).exclusive().max(6).build(), Detection.readDop(Bits.from("0101")).get());
        assertEquals(Range.min(6).exclusive().max(7).build(), Detection.readDop(Bits.from("0110")).get());
        assertEquals(Range.min(7).exclusive().max(8).build(), Detection.readDop(Bits.from("0111")).get());
        assertEquals(Range.min(8).exclusive().max(10).build(), Detection.readDop(Bits.from("1000")).get());
        assertEquals(Range.min(10).exclusive().max(12).build(), Detection.readDop(Bits.from("1001")).get());
        assertEquals(Range.min(12).exclusive().max(15).build(), Detection.readDop(Bits.from("1010")).get());
        assertEquals(Range.min(15).exclusive().max(20).build(), Detection.readDop(Bits.from("1011")).get());
        assertEquals(Range.min(20).exclusive().max(30).build(), Detection.readDop(Bits.from("1100")).get());
        assertEquals(Range.min(30).exclusive().max(50).build(), Detection.readDop(Bits.from("1101")).get());
        assertEquals(Range.min(50).exclusive().build(), Detection.readDop(Bits.from("1110")).get());
        assertFalse(Detection.readDop(Bits.from("1111")).isPresent());
    }

    @Test
    public void testReadRlsType() {
        assertEquals(RlsType.ACKNOWLEDGEMENT_SERVICE, Detection.readRlsType(Bits.from("0001")));
        assertEquals(RlsType.TEST_SERVICE, Detection.readRlsType(Bits.from("1111")));
        assertEquals(RlsType.OTHER, Detection.readRlsType(Bits.from("1000")));
    }

    @Test
    public void testReadRlsProvider() {
        assertEquals(RlsProvider.GALILEO, Detection.readRlsProvider(Bits.from("001")));
        assertEquals(RlsProvider.GLONASS, Detection.readRlsProvider(Bits.from("010")));
        assertEquals(RlsProvider.OTHER, Detection.readRlsProvider(Bits.from("011")));
    }

    @Test
    public void testReadActivationMethod() {
        assertEquals(ActivationMethod.MANUAL_ACTIVATION_BY_USER, Detection.readActivationMethod(Bits.from("00")));
        assertEquals(ActivationMethod.AUTOMATIC_ACTIVATION_BY_BEACON, Detection.readActivationMethod(Bits.from("01")));
        assertEquals(ActivationMethod.AUTOMATIC_ACTIVATION_BY_EXTERNAL_MEANS,
                Detection.readActivationMethod(Bits.from("10")));
        assertEquals(ActivationMethod.OTHER, Detection.readActivationMethod(Bits.from("11")));
    }

    @Test
    public void testReadVesselIdAircraftOperatoAndSerialNumber() {
        // XYZ in short baudot form followed by 15 in 12 bits + 17 unused bits
        Bits b = createVesselIdAircraftOperatorAndSerialNumber();
        AircraftOperatorAndSerialNumber a = Detection.readVesselIdAircraftOperatoAndSerialNumber(b);
        assertEquals("XYZ", a.aircraftOperatorDesignator());
        assertEquals(15, a.serialNumber());
    }

    @Test
    public void testReadVesselIdWithAircraftOperatorAndSerialNumber() {
        AircraftOperatorAndSerialNumber a = (AircraftOperatorAndSerialNumber) Detection
                .readVesselId(Bits.from("101").concatWith(createVesselIdAircraftOperatorAndSerialNumber())).get();
        assertEquals("XYZ", a.aircraftOperatorDesignator());
    }

    @Test
    public void testReadVesselIdUnused() {
        assertFalse(Detection.readVesselId(Bits.from("111").concatWith(ones(44))).isPresent());
    }

    private static Bits createVesselIdAircraftOperatorAndSerialNumber() {
        return Bits.from("101111010110001000000001111" + zeros(17));
    }

    @Test
    public void testReadMmsi() {
        Bits bits = createVesselIdMmsiWithEpirbMmsiBits();
        Mmsi a = Detection.readVesselIdMmsi(bits);
        assertEquals(123456789, (int) a.mmsi().get());
        assertEquals(974454287, (int) a.epirbMmsi().get());
    }

    private static Bits createVesselIdMmsiWithEpirbMmsiBits() {
        String mmsi = leftPadWithZeros(new BigInteger("123456789").toString(2), 30);
        String last4 = leftPadWithZeros(new BigInteger("4287").toString(2), 14);
        Bits bits = Bits.from(mmsi + last4);
        return bits;
    }

    //////////////////////////
    // Hex creation
    //////////////////////////

    @Test
    public void testCreateHexWithMmsiVesselId() {
        Bits b = createBitsWithVesselIdFieldMmsi();
        Detection d = Detection.from(b);
        assertEquals(VesselIdType.MMSI, d.vesselId().get().vesselIdType());
        assertTrue(Json.prettyPrint(d.toJson()).contains("epirbMmsi"));
        Bits b2 = Bits.from("00").concatWith(b);
        String hex = b2.toHex().toUpperCase();
        assertEquals(51, hex.length());
    }

    static Bits createBitsWithVesselIdFieldMmsi() {
        Bits vid = Bits.from("001").concatWith(createVesselIdMmsiWithEpirbMmsiBits());
        return Bits.from(BITS).replace(90, vid);
    }

    @Test
    public void testCreateHexWithAircraftOperatorAndSerialNumber() {
        Bits b = createBitsWithVesselIdFieldAircraftOperatorAndSerialNumber();
        Detection d = Detection.from(b);
        assertEquals(VesselIdType.AIRCRAFT_OPERATOR_AND_SERIAL_NUMBER, d.vesselId().get().vesselIdType());
        assertTrue(Json.prettyPrint(d.toJson()).contains("serialNumber"));
        Bits b2 = Bits.from("00").concatWith(b);
        String hex = b2.toHex().toUpperCase();
        assertEquals(51, hex.length());
    }

    static Bits createBitsWithVesselIdFieldAircraftOperatorAndSerialNumber() {
        Bits vid = Bits.from("101").concatWith(createVesselIdAircraftOperatorAndSerialNumber());
        return Bits.from(BITS).replace(90, vid);
    }

    @Test
    public void testCreateHexWithAircraftRegistrationMarking() {
        Bits b = createBitsWithVesselIdFieldAircraftRegistrationMarkingVhAbc();
        Detection d = Detection.from(b);
        assertEquals(VesselIdType.AIRCRAFT_REGISTRATION_MARKING, d.vesselId().get().vesselIdType());
        Bits b2 = Bits.from("00").concatWith(b);
        String hex = b2.toHex().toUpperCase();
        assertEquals(51, hex.length());
    }

    static Bits createBitsWithVesselIdFieldAircraftRegistrationMarkingVhAbc() {
        Bits vid = Bits.from("011").concatWith(createVesselIdAircraftRegistrationMarkingVhAbc());
        return Bits.from(BITS).replace(90, vid);
    }

    //////////////////////////////////////////////////////////////////////

    @Test
    public void testReadMmsiPrimaryNotPresent() {
        String mmsi = leftPadWithZeros(new BigInteger("111111").toString(2), 30);
        String last4 = leftPadWithZeros(new BigInteger("4287").toString(2), 14);
        Bits bits = Bits.from(mmsi + last4);
        Mmsi a = Detection.readVesselIdMmsi(bits);
        assertFalse(a.mmsi().isPresent());
        assertEquals(974114287, (int) a.epirbMmsi().get());
    }

    @Test
    public void testReadMmsiEpirbMmsiNotPresent() {
        String mmsi = leftPadWithZeros(new BigInteger("123456789").toString(2), 30);
        String last4ish = leftPadWithZeros(new BigInteger("10922").toString(2), 14);
        Bits bits = Bits.from(mmsi + last4ish);
        Mmsi a = Detection.readVesselIdMmsi(bits);
        assertEquals(123456789, (int) a.mmsi().get());
        assertFalse(a.epirbMmsi().isPresent());
    }

    @Test
    public void testReadVesselIdMmsi() {
        Bits b = Bits.from("001").concatWith(createVesselIdMmsiWithEpirbMmsiBits());
        Mmsi a = (Mmsi) Detection.readVesselId(b).get();
        assertEquals(123456789, (int) a.mmsi().get());
    }

    @Test
    public void testBatteryPercentInFlightEmergency() {
        assertEquals(Range.min(0).max(33).build(),
                Detection.readBatteryPercentInFlightEmergency(Bits.from("00")).get());
        assertEquals(Range.min(33).exclusive().max(66).build(),
                Detection.readBatteryPercentInFlightEmergency(Bits.from("01")).get());
        assertEquals(Range.min(66).exclusive().max(100).build(),
                Detection.readBatteryPercentInFlightEmergency(Bits.from("10")).get());
        assertFalse(Detection.readBatteryPercentInFlightEmergency(Bits.from("11")).isPresent());
    }

    @Test
    public void testReadTriggeringEvent() {
        assertEquals(TriggeringEvent.MANUAL_ACTIVATION_BY_CREW, Detection.readTriggeringEvent(Bits.from("0001")));
        assertEquals(TriggeringEvent.G_SWITCH_OR_DEFORMATION_ACTIVATION,
                Detection.readTriggeringEvent(Bits.from("0100")));
        assertEquals(TriggeringEvent.AUTOMATIC_ACTIVATION_FROM_AVIONICS_OR_TRIGGERING_SYSTEM,
                Detection.readTriggeringEvent(Bits.from("1000")));
        assertEquals(TriggeringEvent.OTHER, Detection.readTriggeringEvent(Bits.from("1111")));
    }

    @Test
    public void testReadRotatingFieldCancellationMessage() {
        assertEquals(DeactivationMethod.MANUAL_DEACTIVATION_BY_USER,
                Detection.readRotatingFieldCancellationMessage(Bits.from(ones(42) + "10")).deactivationMethod());
        assertEquals(DeactivationMethod.AUTOMATIC_DEACTIVATION_BY_EXTERNAL_MEANS,
                Detection.readRotatingFieldCancellationMessage(Bits.from(ones(42) + "01")).deactivationMethod());
        assertEquals(DeactivationMethod.OTHER,
                Detection.readRotatingFieldCancellationMessage(Bits.from(ones(42) + "11")).deactivationMethod());
    }

    @Test
    public void testReadRotatingFieldCancellation() {
        Bits b = Bits.from("1111" + ones(42) + "10");
        Cancellation a = (Cancellation) Detection.readRotatingField(b);
        assertEquals(DeactivationMethod.MANUAL_DEACTIVATION_BY_USER, a.deactivationMethod());
    }

    @Test
    public void testReadRotatingFieldNationalUse() {
        Bits b = Bits.from("0011" + ones(44));
        NationalUse a = (NationalUse) Detection.readRotatingField(b);
        assertEquals(ones(44), a.bitString());
    }

    @Test
    public void testReadRotatingFieldUnknown() {
        Bits b = Bits.from("0100" + ones(44));
        UnknownRotatingField a = (UnknownRotatingField) Detection.readRotatingField(b);
        assertEquals(ones(44), a.bitString());
    }

    @Test
    public void testReadPositionNotAvailable() {
        assertFalse(Detection.readPosition(Bits.from("11111111000001111100000111111111111110000011111")).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadPositionWrongSize() {
        Detection.readPosition(Bits.from("11"));
    }

    @Test
    public void testReadTimeOfLastEncodedLocationSecondsOfZero() {
        OffsetTime time = Detection.readTimeOfLastEncodedLocationSeconds(Bits.from(zeros(17)));
        assertEquals(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC), time);
    }

    @Test
    public void testReadTimeOfLastEncodedLocationSecondsOfNonZero() {
        int c = 13;
        int n = new BigInteger(ones(c), 2).intValue();
        int hours = n / 3600;
        int mins = (n - hours * 3600) / 60;
        int secs = n - hours * 3600 - mins * 60;
        OffsetTime time = Detection.readTimeOfLastEncodedLocationSeconds(Bits.from(zeros(17 - c) + ones(c)));
        assertEquals(OffsetTime.of(hours, mins, secs, 0, ZoneOffset.UTC), time);
    }

    @Test
    public void testReadRotatingFieldEltDtInFlightEmergency() {
        Bits b = Bits.from("0001" // rotating field type
                + "00001111111111111" // time
                + "0001111010" // altitude
                + "0100" // triggering event
                + "01" // gnss status
                + "10" // battery
                + "000000000"); // spare
        EltDtInFlightEmergency a = (EltDtInFlightEmergency) Detection.readRotatingField(b);
        assertEquals(OffsetTime.of(2, 16, 31, 0, ZoneOffset.UTC), a.timeOfLastEncodedLocation());
        assertEquals(1952 - 400, a.altitudeEncodedLocationMetres());
        assertEquals(TriggeringEvent.G_SWITCH_OR_DEFORMATION_ACTIVATION, a.triggeringEvent());
        assertEquals(GnssStatus.LOCATION_2D, a.gnssStatus());
        assertEquals(Range.min(66).exclusive().max(100).build(), a.remainingBatteryCapacityPercent().get());
    }

    @Test
    public void testReadBeaconFeedbackWithGalileo() {
        Bits b = Bits.from("1" + "0" + "0001" + "10" + ones(14) + zeros(11));
        BeaconFeedback a = Detection.readBeaconFeadback(b, RlsProvider.GALILEO).get();
        assertTrue(a.rlmType1FeedbackReceived());
        assertFalse(a.rlmType2FeedbackReceived());
        assertEquals(RlsType.ACKNOWLEDGEMENT_SERVICE, a.rlsType());
        assertEquals(ones(13), a.shortRlmParametersBitString().get());
    }

    @Test
    public void testReadBeaconFeedbackWithGalileoTestService() {
        Bits b = Bits.from("1" + "0" + "1111" + "10" + ones(14) + zeros(11));
        BeaconFeedback a = Detection.readBeaconFeadback(b, RlsProvider.GALILEO).get();
        assertTrue(a.rlmType1FeedbackReceived());
        assertFalse(a.rlmType2FeedbackReceived());
        assertEquals(RlsType.TEST_SERVICE, a.rlsType());
        assertEquals("10" + ones(13), a.shortRlmParametersBitString().get());
    }

    @Test
    public void testReadBeaconFeedbackWithGalileoOtherService() {
        Bits b = Bits.from("1" + "0" + "1110" + "10" + ones(14) + zeros(11));
        BeaconFeedback a = Detection.readBeaconFeadback(b, RlsProvider.GALILEO).get();
        assertTrue(a.rlmType1FeedbackReceived());
        assertFalse(a.rlmType2FeedbackReceived());
        assertEquals(RlsType.OTHER, a.rlsType());
        assertEquals("10" + ones(13), a.shortRlmParametersBitString().get());
    }

    @Test
    public void testReadBeaconFeedbackWithGlonass() {
        Bits b = Bits.from("1" + "0" + "1110" + "10" + ones(14) + zeros(11));
        assertFalse(Detection.readBeaconFeadback(b, RlsProvider.GLONASS).isPresent());
    }

    @Test
    public void testReadRotatingFieldRls() {
        Bits b = Bits.from("0010" // rotating field id
                + "00" // empty
                + "1" //
                + "0" //
                + zeros(4) //
                + "010" // glonass
                + "1" //
                + "0" //
                + "1110" //
                + "10" + ones(14) + zeros(11));
        Rls a = (Rls) Detection.readRotatingField(b);
        assertTrue(a.canProcessAutomaticallyGeneratedAckRlmType1());
        assertFalse(a.canProcessManuallyGeneratedRlm());
        assertEquals(RlsProvider.GLONASS, a.rlsProvider());
        assertFalse(a.beaconFeedback().isPresent());
    }

    @Test
    public void testReadLocationPositiveLatPositiveLon() {
        Bits bits = Bits.from("00110000110010110000110001000101000000100011111");
        EncodedGnssPosition p = Detection.readPosition(bits).get();
        assertEquals(48.79315185546875, p.lat(), 0.0000001);
        assertEquals(69.00875854492188, p.lon(), 0.0000001);
    }

    @Test
    public void testDetectionBchCalculation() {
        Detection d = Detection.fromHexGroundSegmentRepresentation(SAMPLE_HEX);
        assertEquals("010010010010101001001111110001010111101001001001",
                d.calculateBchErrorCorrectionCode().toBitString());
    }

    @Test
    public void testReadLocationNegativeLatNegativeLon() {
        Bits bits = Bits.from("10110000110010110000110101000101000000100011111");
        EncodedGnssPosition p = Detection.readPosition(bits).get();
        assertEquals(-48.79315185546875, p.lat(), 0.0000001);
        assertEquals(-69.00875854492188, p.lon(), 0.0000001);
    }

    private static String leftPadWithZeros(String s, int length) {
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            long t = System.currentTimeMillis();
            long n = 1000000;
            for (long j = 0; j < n; j++) {
                Detection.fromHexGroundSegmentRepresentation(SAMPLE_HEX);
            }
            System.out.println("rate=" + new DecimalFormat("0.00").format(n * 1000.0 / (System.currentTimeMillis() - t))
                    + "msg/s");
        }

    }
}
