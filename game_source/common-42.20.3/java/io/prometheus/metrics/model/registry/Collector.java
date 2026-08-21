/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.function.Predicate;

@FunctionalInterface
public interface Collector {
    public MetricSnapshot collect();

    default public MetricSnapshot collect(PrometheusScrapeRequest scrapeRequest) {
        return this.collect();
    }

    default public MetricSnapshot collect(Predicate<String> includedNames) {
        MetricSnapshot result = this.collect();
        if (includedNames.test(result.getMetadata().getPrometheusName())) {
            return result;
        }
        return null;
    }

    default public MetricSnapshot collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
        MetricSnapshot result = this.collect(scrapeRequest);
        if (includedNames.test(result.getMetadata().getPrometheusName())) {
            return result;
        }
        return null;
    }

    default public String getPrometheusName() {
        return null;
    }
}

