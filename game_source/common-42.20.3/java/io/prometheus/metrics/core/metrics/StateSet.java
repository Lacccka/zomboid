/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.StateSetDataPoint;
import io.prometheus.metrics.core.metrics.StatefulMetric;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class StateSet
extends StatefulMetric<StateSetDataPoint, DataPoint>
implements StateSetDataPoint {
    private final boolean exemplarsEnabled;
    private final String[] names;

    private StateSet(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricsProperties[] properties = this.getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = this.getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
        for (String name : this.names = builder.names) {
            if (!this.getMetadata().getPrometheusName().equals(PrometheusNaming.prometheusName(name))) continue;
            throw new IllegalArgumentException("Label name " + name + " is illegal (can't use the metric name as label name in state set metrics)");
        }
    }

    @Override
    public StateSetSnapshot collect() {
        return (StateSetSnapshot)super.collect();
    }

    protected StateSetSnapshot collect(List<Labels> labels, List<DataPoint> metricDataList) {
        ArrayList<StateSetSnapshot.StateSetDataPointSnapshot> data = new ArrayList<StateSetSnapshot.StateSetDataPointSnapshot>(labels.size());
        for (int i = 0; i < labels.size(); ++i) {
            data.add(new StateSetSnapshot.StateSetDataPointSnapshot(this.names, metricDataList.get(i).values, labels.get(i)));
        }
        return new StateSetSnapshot(this.getMetadata(), (Collection<StateSetSnapshot.StateSetDataPointSnapshot>)data);
    }

    @Override
    public void setTrue(String state) {
        ((DataPoint)this.getNoLabels()).setTrue(state);
    }

    @Override
    public void setFalse(String state) {
        ((DataPoint)this.getNoLabels()).setFalse(state);
    }

    @Override
    protected DataPoint newDataPoint() {
        return new DataPoint();
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder
    extends StatefulMetric.Builder<Builder, StateSet> {
        private String[] names;

        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        public Builder states(Class<? extends Enum<?>> enumClass) {
            return this.states((String[])Stream.of(enumClass.getEnumConstants()).map(Enum::toString).toArray(String[]::new));
        }

        public Builder states(String ... stateNames) {
            if (stateNames.length == 0) {
                throw new IllegalArgumentException("states cannot be empty");
            }
            this.names = (String[])Stream.of(stateNames).distinct().sorted().toArray(String[]::new);
            return this;
        }

        @Override
        public StateSet build() {
            if (this.names == null) {
                throw new IllegalStateException("State names are required when building a StateSet.");
            }
            return new StateSet(this, this.properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    class DataPoint
    implements StateSetDataPoint {
        private final boolean[] values;

        private DataPoint() {
            this.values = new boolean[StateSet.this.names.length];
        }

        @Override
        public void setTrue(String state) {
            this.set(state, true);
        }

        @Override
        public void setFalse(String state) {
            this.set(state, false);
        }

        private void set(String name, boolean value) {
            for (int i = 0; i < StateSet.this.names.length; ++i) {
                if (!StateSet.this.names[i].equals(name)) continue;
                this.values[i] = value;
                return;
            }
            throw new IllegalArgumentException(name + ": unknown state");
        }
    }
}

