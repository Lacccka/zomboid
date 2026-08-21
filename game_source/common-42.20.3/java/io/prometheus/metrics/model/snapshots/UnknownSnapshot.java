/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class UnknownSnapshot
extends MetricSnapshot {
    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownDataPointSnapshot> data) {
        super(metadata, data);
    }

    public List<UnknownDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<UnknownDataPointSnapshot> dataPoints = new ArrayList<UnknownDataPointSnapshot>();

        private Builder() {
        }

        public Builder dataPoint(UnknownDataPointSnapshot data) {
            this.dataPoints.add(data);
            return this;
        }

        @Override
        public UnknownSnapshot build() {
            return new UnknownSnapshot(this.buildMetadata(), (Collection<UnknownDataPointSnapshot>)this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static final class UnknownDataPointSnapshot
    extends DataPointSnapshot {
        private final double value;
        private final Exemplar exemplar;

        public UnknownDataPointSnapshot(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0L);
        }

        public UnknownDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
            this.value = value;
            this.exemplar = exemplar;
        }

        public double getValue() {
            return this.value;
        }

        public Exemplar getExemplar() {
            return this.exemplar;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DataPointSnapshot.Builder<Builder> {
            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {
            }

            public Builder value(double value) {
                this.value = value;
                return this;
            }

            public Builder exemplar(Exemplar exemplar) {
                this.exemplar = exemplar;
                return this;
            }

            public UnknownDataPointSnapshot build() {
                if (this.value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new UnknownDataPointSnapshot(this.value, this.labels, this.exemplar, this.scrapeTimestampMillis);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }
}

