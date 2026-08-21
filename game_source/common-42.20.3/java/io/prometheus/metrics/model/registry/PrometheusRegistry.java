/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class PrometheusRegistry {
    public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();
    private final Set<String> prometheusNames = ConcurrentHashMap.newKeySet();
    private final List<Collector> collectors = new CopyOnWriteArrayList<Collector>();
    private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<MultiCollector>();

    public void register(Collector collector) {
        String prometheusName = collector.getPrometheusName();
        if (prometheusName != null && !this.prometheusNames.add(prometheusName)) {
            throw new IllegalStateException("Can't register " + prometheusName + " because a metric with that name is already registered.");
        }
        this.collectors.add(collector);
    }

    public void register(MultiCollector collector) {
        for (String prometheusName : collector.getPrometheusNames()) {
            if (this.prometheusNames.add(prometheusName)) continue;
            throw new IllegalStateException("Can't register " + prometheusName + " because that name is already registered.");
        }
        this.multiCollectors.add(collector);
    }

    public void unregister(Collector collector) {
        this.collectors.remove(collector);
        String prometheusName = collector.getPrometheusName();
        if (prometheusName != null) {
            this.prometheusNames.remove(collector.getPrometheusName());
        }
    }

    public void unregister(MultiCollector collector) {
        this.multiCollectors.remove(collector);
        for (String prometheusName : collector.getPrometheusNames()) {
            this.prometheusNames.remove(PrometheusNaming.prometheusName(prometheusName));
        }
    }

    public void clear() {
        this.collectors.clear();
        this.multiCollectors.clear();
        this.prometheusNames.clear();
    }

    public MetricSnapshots scrape() {
        return this.scrape((PrometheusScrapeRequest)null);
    }

    public MetricSnapshots scrape(PrometheusScrapeRequest scrapeRequest) {
        MetricSnapshots.Builder result = MetricSnapshots.builder();
        for (Collector collector : this.collectors) {
            MetricSnapshot snapshot = scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
            if (snapshot == null) continue;
            if (result.containsMetricName(snapshot.getMetadata().getName())) {
                throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
            }
            result.metricSnapshot(snapshot);
        }
        for (MultiCollector multiCollector : this.multiCollectors) {
            MetricSnapshots snapshots2 = scrapeRequest == null ? multiCollector.collect() : multiCollector.collect(scrapeRequest);
            for (MetricSnapshot snapshot : snapshots2) {
                if (result.containsMetricName(snapshot.getMetadata().getName())) {
                    throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
                }
                result.metricSnapshot(snapshot);
            }
        }
        return result.build();
    }

    public MetricSnapshots scrape(Predicate<String> includedNames) {
        if (includedNames == null) {
            return this.scrape();
        }
        return this.scrape(includedNames, null);
    }

    public MetricSnapshots scrape(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
        if (includedNames == null) {
            return this.scrape(scrapeRequest);
        }
        MetricSnapshots.Builder result = MetricSnapshots.builder();
        for (Collector collector : this.collectors) {
            MetricSnapshot snapshot;
            String prometheusName = collector.getPrometheusName();
            if (prometheusName != null && !includedNames.test(prometheusName) || (snapshot = scrapeRequest == null ? collector.collect(includedNames) : collector.collect(includedNames, scrapeRequest)) == null) continue;
            result.metricSnapshot(snapshot);
        }
        for (MultiCollector multiCollector : this.multiCollectors) {
            List<String> prometheusNames = multiCollector.getPrometheusNames();
            boolean excluded = !prometheusNames.isEmpty();
            for (String prometheusName : prometheusNames) {
                if (!includedNames.test(prometheusName)) continue;
                excluded = false;
                break;
            }
            if (excluded) continue;
            MetricSnapshots snapshots2 = scrapeRequest == null ? multiCollector.collect(includedNames) : multiCollector.collect(includedNames, scrapeRequest);
            for (MetricSnapshot snapshot : snapshots2) {
                if (snapshot == null) continue;
                result.metricSnapshot(snapshot);
            }
        }
        return result.build();
    }
}

