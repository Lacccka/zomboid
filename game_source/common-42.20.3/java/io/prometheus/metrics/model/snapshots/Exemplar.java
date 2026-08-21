/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Labels;

public class Exemplar {
    public static final String TRACE_ID = "trace_id";
    public static final String SPAN_ID = "span_id";
    private final double value;
    private final Labels labels;
    private final long timestampMillis;

    public Exemplar(double value, Labels labels, long timestampMillis) {
        if (labels == null) {
            throw new NullPointerException("Labels cannot be null. Use Labels.EMPTY.");
        }
        this.value = value;
        this.labels = labels;
        this.timestampMillis = timestampMillis;
    }

    public double getValue() {
        return this.value;
    }

    public Labels getLabels() {
        return this.labels;
    }

    public boolean hasTimestamp() {
        return this.timestampMillis != 0L;
    }

    public long getTimestampMillis() {
        return this.timestampMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double value = null;
        private Labels labels = Labels.EMPTY;
        private String traceId = null;
        private String spanId = null;
        private long timestampMillis = 0L;

        private Builder() {
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder labels(Labels labels) {
            if (labels == null) {
                throw new NullPointerException();
            }
            this.labels = labels;
            return this;
        }

        public Builder timestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        public Exemplar build() {
            if (this.value == null) {
                throw new IllegalStateException("cannot build an Exemplar without a value");
            }
            Labels allLabels = this.traceId != null && this.spanId != null ? Labels.of(Exemplar.TRACE_ID, this.traceId, Exemplar.SPAN_ID, this.spanId) : (this.traceId != null ? Labels.of(Exemplar.TRACE_ID, this.traceId) : (this.spanId != null ? Labels.of(Exemplar.SPAN_ID, this.spanId) : Labels.EMPTY));
            if (!this.labels.isEmpty()) {
                allLabels = allLabels.merge(this.labels);
            }
            return new Exemplar(this.value, allLabels, this.timestampMillis);
        }
    }
}

