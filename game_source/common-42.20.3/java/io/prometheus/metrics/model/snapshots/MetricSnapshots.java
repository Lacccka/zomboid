/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MetricSnapshots
implements Iterable<MetricSnapshot> {
    private final List<MetricSnapshot> snapshots;

    public MetricSnapshots(MetricSnapshot ... snapshots2) {
        this(Arrays.asList(snapshots2));
    }

    public MetricSnapshots(Collection<MetricSnapshot> snapshots2) {
        ArrayList<MetricSnapshot> list = new ArrayList<MetricSnapshot>(snapshots2);
        list.sort(Comparator.comparing(s -> s.getMetadata().getPrometheusName()));
        for (int i = 0; i < snapshots2.size() - 1; ++i) {
            if (!((MetricSnapshot)list.get(i)).getMetadata().getPrometheusName().equals(((MetricSnapshot)list.get(i + 1)).getMetadata().getPrometheusName())) continue;
            throw new IllegalArgumentException(((MetricSnapshot)list.get(i)).getMetadata().getPrometheusName() + ": duplicate metric name");
        }
        this.snapshots = Collections.unmodifiableList(list);
    }

    public static MetricSnapshots of(MetricSnapshot ... snapshots2) {
        return new MetricSnapshots(snapshots2);
    }

    @Override
    public Iterator<MetricSnapshot> iterator() {
        return this.snapshots.iterator();
    }

    public int size() {
        return this.snapshots.size();
    }

    public MetricSnapshot get(int i) {
        return this.snapshots.get(i);
    }

    public Stream<MetricSnapshot> stream() {
        return this.snapshots.stream();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<MetricSnapshot> snapshots = new ArrayList<MetricSnapshot>();
        private final Set<String> prometheusNames = new HashSet<String>();

        private Builder() {
        }

        public boolean containsMetricName(String name) {
            if (name == null) {
                return false;
            }
            String prometheusName = PrometheusNaming.prometheusName(name);
            return this.prometheusNames.contains(prometheusName);
        }

        public Builder metricSnapshot(MetricSnapshot snapshot) {
            this.snapshots.add(snapshot);
            this.prometheusNames.add(snapshot.getMetadata().getPrometheusName());
            return this;
        }

        public MetricSnapshots build() {
            return new MetricSnapshots(this.snapshots);
        }
    }
}

