package au.gov.amsa.sgb.decoder.rotatingfield;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class Range {

    private final Optional<RangeEnd> min;
    private final Optional<RangeEnd> max;

    private Range(Optional<RangeEnd> min, Optional<RangeEnd> max) {
        Preconditions.checkNotNull(min);
        Preconditions.checkNotNull(max);
        this.min = min;
        this.max = max;
    }

    public static BuilderMin min(int value) {
        return new BuilderMin(new RangeEnd(value, false), Optional.empty());
    }

    public static BuilderMax max(int value) {
        return new BuilderMax(Optional.empty(), new RangeEnd(value, false));
    }

    public static Range unlimited() {
        return new Range(Optional.empty(), Optional.empty());
    }

    public static final class BuilderMin {
        private RangeEnd min;
        private Optional<RangeEnd> max;

        BuilderMin(RangeEnd min, Optional<RangeEnd> max) {
            this.min = min;
            this.max = max;
        }

        public BuilderMin exclusive() {
            this.min = new RangeEnd(min.value(), true);
            return this;
        }

        public BuilderMax max(int value) {
            return new BuilderMax(Optional.of(min), new RangeEnd(value, false));
        }

        public Range build() {
            return new Range(Optional.of(min), max);
        }
    }

    public static final class BuilderMax {
        private Optional<RangeEnd> min;
        private RangeEnd max;

        BuilderMax(Optional<RangeEnd> min, RangeEnd max) {
            this.min = min;
            this.max = max;
        }

        public Range exclusive() {
            this.max = new RangeEnd(max.value(), true);
            return this.build();
        }

        public Range build() {
            return new Range(min, Optional.of(max));
        }

    }

    public Optional<RangeEnd> min() {
        return min;
    }

    public Optional<RangeEnd> max() {
        return max;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + max.hashCode();
        result = prime * result + min.hashCode();
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
        Range other = (Range) obj;
        return min.equals(other.min()) && max.equals(other.max());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Range [");
        if (min.isPresent()) {
            b.append("min=");
            b.append(min.get().value());
            b.append(", minExclusive=" + min.get().isExclusive());
        }
        if (max.isPresent()) {
            if (min.isPresent()) {
                b.append(", ");
            }
            b.append("max=");
            b.append(max.get().value());
            b.append(", maxExclusive=" + max.get().isExclusive());
        }
        b.append("]");
        return b.toString();
    }

}
