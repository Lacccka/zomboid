/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@FunctionalInterface
public interface MultiCollector {
    public MetricSnapshots collect();

    default public MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
        return this.collect();
    }

    default public MetricSnapshots collect(Predicate<String> includedNames) {
        return this.collect(includedNames, null);
    }

    default public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
        MetricSnapshots allSnapshots = scrapeRequest == null ? this.collect() : this.collect(scrapeRequest);
        MetricSnapshots.Builder result = MetricSnapshots.builder();
        for (MetricSnapshot snapshot : allSnapshots) {
            if (!includedNames.test(snapshot.getMetadata().getPrometheusName())) continue;
            result.metricSnapshot(snapshot);
        }
        return result.build();
    }

    default public List<String> getPrometheusNames() {
        return Collections.emptyList();
    }
}

