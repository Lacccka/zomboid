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

public class CounterSnapshot
extends MetricSnapshot {
    public CounterSnapshot(MetricMetadata metadata, Collection<CounterDataPointSnapshot> dataPoints) {
        super(metadata, dataPoints);
    }

    public List<CounterDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<CounterDataPointSnapshot> dataPoints = new ArrayList<CounterDataPointSnapshot>();

        private Builder() {
        }

        public Builder dataPoint(CounterDataPointSnapshot dataPoint) {
            this.dataPoints.add(dataPoint);
            return this;
        }

        @Override
        public CounterSnapshot build() {
            return new CounterSnapshot(this.buildMetadata(), (Collection<CounterDataPointSnapshot>)this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static class CounterDataPointSnapshot
    extends DataPointSnapshot {
        private final double value;
        private final Exemplar exemplar;

        public CounterDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0L);
        }

        public CounterDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(labels, createdTimestampMillis, scrapeTimestampMillis);
            this.value = value;
            this.exemplar = exemplar;
            this.validate();
        }

        public double getValue() {
            return this.value;
        }

        public Exemplar getExemplar() {
            return this.exemplar;
        }

        protected void validate() {
            if (this.value < 0.0) {
                throw new IllegalArgumentException(this.value + ": counters cannot have a negative value");
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DataPointSnapshot.Builder<Builder> {
            private Exemplar exemplar = null;
            private Double value = null;
            private long createdTimestampMillis = 0L;

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

            public Builder createdTimestampMillis(long createdTimestampMillis) {
                this.createdTimestampMillis = createdTimestampMillis;
                return this;
            }

            public CounterDataPointSnapshot build() {
                if (this.value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new CounterDataPointSnapshot(this.value, this.labels, this.exemplar, this.createdTimestampMillis, this.scrapeTimestampMillis);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }
}

