/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Labels;

public abstract class DataPointSnapshot {
    private final Labels labels;
    private final long createdTimestampMillis;
    private final long scrapeTimestampMillis;

    protected DataPointSnapshot(Labels labels, long createdTimestampMillis, long scrapeTimestampMillis) {
        this.labels = labels;
        this.createdTimestampMillis = createdTimestampMillis;
        this.scrapeTimestampMillis = scrapeTimestampMillis;
        this.validate();
    }

    private void validate() {
        if (this.labels == null) {
            throw new IllegalArgumentException("Labels cannot be null. Use Labels.EMPTY if there are no labels.");
        }
        if (this.createdTimestampMillis < 0L) {
            throw new IllegalArgumentException("Created timestamp cannot be negative. Use 0 if the metric doesn't have a created timestamp.");
        }
        if (this.scrapeTimestampMillis < 0L) {
            throw new IllegalArgumentException("Scrape timestamp cannot be negative. Use 0 to indicate that the Prometheus server should set the scrape timestamp.");
        }
        if (this.hasCreatedTimestamp() && this.hasScrapeTimestamp() && this.scrapeTimestampMillis < this.createdTimestampMillis) {
            throw new IllegalArgumentException("The scrape timestamp cannot be before the created timestamp");
        }
    }

    public Labels getLabels() {
        return this.labels;
    }

    public boolean hasScrapeTimestamp() {
        return this.scrapeTimestampMillis != 0L;
    }

    public long getScrapeTimestampMillis() {
        return this.scrapeTimestampMillis;
    }

    public boolean hasCreatedTimestamp() {
        return this.createdTimestampMillis != 0L;
    }

    public long getCreatedTimestampMillis() {
        return this.createdTimestampMillis;
    }

    public static abstract class Builder<T extends Builder<T>> {
        protected Labels labels = Labels.EMPTY;
        protected long scrapeTimestampMillis = 0L;

        public T labels(Labels labels) {
            this.labels = labels;
            return this.self();
        }

        public T scrapeTimestampMillis(long scrapeTimestampMillis) {
            this.scrapeTimestampMillis = scrapeTimestampMillis;
            return this.self();
        }

        protected abstract T self();
    }
}

