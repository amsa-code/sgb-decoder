package au.gov.amsa.sgb.decoder.rotatingfield;

public final class RangeEnd {

    private final int value;
    private final boolean exclusive;

    public RangeEnd(int value, boolean exclusive) {
        this.value = value;
        this.exclusive = exclusive;
    }

    public int value() {
        return value;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (exclusive ? 1231 : 1237);
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RangeEnd other = (RangeEnd) obj;
        if (exclusive != other.exclusive)
            return false;
        if (value != other.value)
            return false;
        return true;
    }

}
