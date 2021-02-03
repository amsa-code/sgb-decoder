package au.gov.amsa.sgb.decoder.rotatingfield;

import java.util.Optional;

public final class BeaconFeedback {

    private final boolean rlmType1FeedbackReceived;
    private final boolean rlmType2FeedbackReceived;
    private final RlsType rlsType;
    private final Optional<String> shortRlmParametersBitString;

    public BeaconFeedback(boolean rlmType1FeedbackReceived, boolean rlmType2FeedbackReceived, RlsType rlsType,
            Optional<String> shortRlmParametersBitString) {
        this.rlmType1FeedbackReceived = rlmType1FeedbackReceived;
        this.rlmType2FeedbackReceived = rlmType2FeedbackReceived;
        this.rlsType = rlsType;
        this.shortRlmParametersBitString = shortRlmParametersBitString;
    }

    public boolean rlmType1FeedbackReceived() {
        return rlmType1FeedbackReceived;
    }

    public boolean rlmType2FeedbackReceived() {
        return rlmType2FeedbackReceived;
    }

    public RlsType rlsType() {
        return rlsType;
    }

    public Optional<String> shortRlmParametersBitString() {
        return shortRlmParametersBitString;
    }

}
