/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class StateSetSnapshot
extends MetricSnapshot {
    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetDataPointSnapshot> data) {
        super(metadata, data);
        this.validate();
    }

    private void validate() {
        if (this.getMetadata().hasUnit()) {
            throw new IllegalArgumentException("An state set metric cannot have a unit.");
        }
        for (StateSetDataPointSnapshot entry : this.getDataPoints()) {
            if (!entry.getLabels().contains(this.getMetadata().getPrometheusName())) continue;
            throw new IllegalArgumentException("Label name " + this.getMetadata().getPrometheusName() + " is reserved.");
        }
    }

    public List<StateSetDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class StateSetDataPointSnapshot
    extends DataPointSnapshot
    implements Iterable<State> {
        private final String[] names;
        private final boolean[] values;

        public StateSetDataPointSnapshot(String[] names, boolean[] values2, Labels labels) {
            this(names, values2, labels, 0L);
        }

        public StateSetDataPointSnapshot(String[] names, boolean[] values2, Labels labels, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
            if (names.length == 0) {
                throw new IllegalArgumentException("StateSet must have at least one state.");
            }
            if (names.length != values2.length) {
                throw new IllegalArgumentException("names[] and values[] must have the same length");
            }
            String[] namesCopy = Arrays.copyOf(names, names.length);
            boolean[] valuesCopy = Arrays.copyOf(values2, names.length);
            StateSetDataPointSnapshot.sort(namesCopy, valuesCopy);
            this.names = namesCopy;
            this.values = valuesCopy;
            this.validate();
        }

        public int size() {
            return this.names.length;
        }

        public String getName(int i) {
            return this.names[i];
        }

        public boolean isTrue(int i) {
            return this.values[i];
        }

        private void validate() {
            for (int i = 0; i < this.names.length; ++i) {
                if (this.names[i].isEmpty()) {
                    throw new IllegalArgumentException("Empty string as state name");
                }
                if (i <= 0 || !this.names[i - 1].equals(this.names[i])) continue;
                throw new IllegalArgumentException(this.names[i] + " duplicate state name");
            }
        }

        private List<State> asList() {
            ArrayList<State> result = new ArrayList<State>(this.size());
            for (int i = 0; i < this.names.length; ++i) {
                result.add(new State(this.names[i], this.values[i]));
            }
            return Collections.unmodifiableList(result);
        }

        @Override
        public Iterator<State> iterator() {
            return this.asList().iterator();
        }

        public Stream<State> stream() {
            return this.asList().stream();
        }

        private static void sort(String[] names, boolean[] values2) {
            int n = names.length;
            for (int i = 0; i < n - 1; ++i) {
                for (int j = 0; j < n - i - 1; ++j) {
                    if (names[j].compareTo(names[j + 1]) <= 0) continue;
                    StateSetDataPointSnapshot.swap(j, j + 1, names, values2);
                }
            }
        }

        private static void swap(int i, int j, String[] names, boolean[] values2) {
            String tmpName = names[j];
            names[j] = names[i];
            names[i] = tmpName;
            boolean tmpValue = values2[j];
            values2[j] = values2[i];
            values2[i] = tmpValue;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DataPointSnapshot.Builder<Builder> {
            private final List<String> names = new ArrayList<String>();
            private final List<Boolean> values = new ArrayList<Boolean>();

            private Builder() {
            }

            public Builder state(String name, boolean value) {
                this.names.add(name);
                this.values.add(value);
                return this;
            }

            @Override
            protected Builder self() {
                return this;
            }

            public StateSetDataPointSnapshot build() {
                boolean[] valuesArray = new boolean[this.values.size()];
                for (int i = 0; i < this.values.size(); ++i) {
                    valuesArray[i] = this.values.get(i);
                }
                return new StateSetDataPointSnapshot(this.names.toArray(new String[0]), valuesArray, this.labels, this.scrapeTimestampMillis);
            }
        }
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<StateSetDataPointSnapshot> dataPoints = new ArrayList<StateSetDataPointSnapshot>();

        private Builder() {
        }

        public Builder dataPoint(StateSetDataPointSnapshot dataPoint) {
            this.dataPoints.add(dataPoint);
            return this;
        }

        @Override
        public Builder unit(Unit unit) {
            throw new IllegalArgumentException("StateSet metric cannot have a unit.");
        }

        @Override
        public StateSetSnapshot build() {
            return new StateSetSnapshot(this.buildMetadata(), (Collection<StateSetDataPointSnapshot>)this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static class State {
        private final String name;
        private final boolean value;

        private State(String name, boolean value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public boolean isTrue() {
            return this.value;
        }
    }
}

