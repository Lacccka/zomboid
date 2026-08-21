/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DistributionDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.Quantiles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SummarySnapshot
extends MetricSnapshot {
    public SummarySnapshot(MetricMetadata metadata, Collection<SummaryDataPointSnapshot> data) {
        super(metadata, data);
    }

    public List<SummaryDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<SummaryDataPointSnapshot> dataPoints = new ArrayList<SummaryDataPointSnapshot>();

        private Builder() {
        }

        public Builder dataPoint(SummaryDataPointSnapshot data) {
            this.dataPoints.add(data);
            return this;
        }

        @Override
        public SummarySnapshot build() {
            return new SummarySnapshot(this.buildMetadata(), (Collection<SummaryDataPointSnapshot>)this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static final class SummaryDataPointSnapshot
    extends DistributionDataPointSnapshot {
        private final Quantiles quantiles;

        public SummaryDataPointSnapshot(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, quantiles, labels, exemplars, createdTimestampMillis, 0L);
        }

        public SummaryDataPointSnapshot(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, scrapeTimestampMillis);
            this.quantiles = quantiles;
            this.validate();
        }

        public Quantiles getQuantiles() {
            return this.quantiles;
        }

        private void validate() {
            for (Label label : this.getLabels()) {
                if (!label.getName().equals("quantile")) continue;
                throw new IllegalArgumentException("quantile is a reserved label name for summaries");
            }
            if (this.quantiles == null) {
                throw new NullPointerException();
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DistributionDataPointSnapshot.Builder<Builder> {
            private Quantiles quantiles = Quantiles.EMPTY;

            private Builder() {
            }

            @Override
            protected Builder self() {
                return this;
            }

            public Builder quantiles(Quantiles quantiles) {
                this.quantiles = quantiles;
                return this;
            }

            @Override
            public Builder count(long count) {
                super.count(count);
                return this;
            }

            public SummaryDataPointSnapshot build() {
                return new SummaryDataPointSnapshot(this.count, this.sum, this.quantiles, this.labels, this.exemplars, this.createdTimestampMillis, this.scrapeTimestampMillis);
            }
        }
    }
}

