/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Unit;

public final class MetricMetadata {
    private final String name;
    private final String prometheusName;
    private final String help;
    private final Unit unit;

    public MetricMetadata(String name) {
        this(name, null, null);
    }

    public MetricMetadata(String name, String help) {
        this(name, help, null);
    }

    public MetricMetadata(String name, String help, Unit unit) {
        this.name = name;
        this.help = help;
        this.unit = unit;
        this.validate();
        this.prometheusName = name.contains(".") ? PrometheusNaming.prometheusName(name) : name;
    }

    public String getName() {
        return this.name;
    }

    public String getPrometheusName() {
        return this.prometheusName;
    }

    public String getHelp() {
        return this.help;
    }

    public boolean hasUnit() {
        return this.unit != null;
    }

    public Unit getUnit() {
        return this.unit;
    }

    private void validate() {
        if (this.name == null) {
            throw new IllegalArgumentException("Missing required field: name is null");
        }
        String error = PrometheusNaming.validateMetricName(this.name);
        if (error != null) {
            throw new IllegalArgumentException("'" + this.name + "': Illegal metric name. " + error + " Call " + PrometheusNaming.class.getSimpleName() + ".sanitizeMetricName(name) to avoid this error.");
        }
        if (this.hasUnit() && !this.name.endsWith("_" + this.unit) && !this.name.endsWith("." + this.unit)) {
            throw new IllegalArgumentException("'" + this.name + "': Illegal metric name. If the unit is non-null, the name must end with the unit: _" + this.unit + ". Call " + PrometheusNaming.class.getSimpleName() + ".sanitizeMetricName(name, unit) to avoid this error.");
        }
    }
}

