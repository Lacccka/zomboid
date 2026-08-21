/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;

public class DuplicateLabelsException
extends IllegalArgumentException {
    private static final long serialVersionUID = 0L;
    private final MetricMetadata metadata;
    private final Labels labels;

    public DuplicateLabelsException(MetricMetadata metadata, Labels labels) {
        super("Duplicate labels for metric \"" + metadata.getName() + "\": " + labels);
        this.metadata = metadata;
        this.labels = labels;
    }

    public MetricMetadata getMetadata() {
        return this.metadata;
    }

    public Labels getLabels() {
        return this.labels;
    }
}

