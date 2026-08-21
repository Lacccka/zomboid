/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InfoSnapshot
extends MetricSnapshot {
    public InfoSnapshot(MetricMetadata metadata, Collection<InfoDataPointSnapshot> data) {
        super(metadata, data);
        if (metadata.hasUnit()) {
            throw new IllegalArgumentException("An Info metric cannot have a unit.");
        }
    }

    public List<InfoDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<InfoDataPointSnapshot> dataPoints = new ArrayList<InfoDataPointSnapshot>();

        private Builder() {
        }

        public Builder dataPoint(InfoDataPointSnapshot dataPoint) {
            this.dataPoints.add(dataPoint);
            return this;
        }

        @Override
        public Builder unit(Unit unit) {
            throw new IllegalArgumentException("Info metric cannot have a unit.");
        }

        @Override
        public InfoSnapshot build() {
            return new InfoSnapshot(this.buildMetadata(), (Collection<InfoDataPointSnapshot>)this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static class InfoDataPointSnapshot
    extends DataPointSnapshot {
        public InfoDataPointSnapshot(Labels labels) {
            this(labels, 0L);
        }

        public InfoDataPointSnapshot(Labels labels, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DataPointSnapshot.Builder<Builder> {
            private Builder() {
            }

            public InfoDataPointSnapshot build() {
                return new InfoDataPointSnapshot(this.labels, this.scrapeTimestampMillis);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }
}

