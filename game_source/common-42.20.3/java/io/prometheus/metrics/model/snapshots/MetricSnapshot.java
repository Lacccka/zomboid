/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.DuplicateLabelsException;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class MetricSnapshot {
    private final MetricMetadata metadata;
    protected final List<? extends DataPointSnapshot> dataPoints;

    protected MetricSnapshot(MetricMetadata metadata, Collection<? extends DataPointSnapshot> dataPoints) {
        if (metadata == null) {
            throw new NullPointerException("metadata");
        }
        if (dataPoints == null) {
            throw new NullPointerException("dataPoints");
        }
        this.metadata = metadata;
        ArrayList<? extends DataPointSnapshot> dataCopy = new ArrayList<DataPointSnapshot>(dataPoints);
        dataCopy.sort(Comparator.comparing(DataPointSnapshot::getLabels));
        this.dataPoints = Collections.unmodifiableList(dataCopy);
        MetricSnapshot.validateLabels(this.dataPoints, metadata);
    }

    public MetricMetadata getMetadata() {
        return this.metadata;
    }

    public abstract List<? extends DataPointSnapshot> getDataPoints();

    private static <T extends DataPointSnapshot> void validateLabels(List<T> dataPoints, MetricMetadata metadata) {
        for (int i = 0; i < dataPoints.size() - 1; ++i) {
            if (!((DataPointSnapshot)dataPoints.get(i)).getLabels().equals(((DataPointSnapshot)dataPoints.get(i + 1)).getLabels())) continue;
            throw new DuplicateLabelsException(metadata, ((DataPointSnapshot)dataPoints.get(i)).getLabels());
        }
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String name;
        private String help;
        private Unit unit;

        public T name(String name) {
            this.name = name;
            return this.self();
        }

        public T help(String help) {
            this.help = help;
            return this.self();
        }

        public T unit(Unit unit) {
            this.unit = unit;
            return this.self();
        }

        public abstract MetricSnapshot build();

        protected MetricMetadata buildMetadata() {
            return new MetricMetadata(this.name, this.help, this.unit);
        }

        protected abstract T self();
    }
}

