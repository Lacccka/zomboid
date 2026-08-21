/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Exemplar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Exemplars
implements Iterable<Exemplar> {
    public static final Exemplars EMPTY = new Exemplars(Collections.emptyList());
    private final List<Exemplar> exemplars;

    private Exemplars(Collection<Exemplar> exemplars) {
        ArrayList<Exemplar> copy = new ArrayList<Exemplar>(exemplars.size());
        for (Exemplar exemplar : exemplars) {
            if (exemplar == null) {
                throw new NullPointerException("Illegal null value in Exemplars");
            }
            copy.add(exemplar);
        }
        this.exemplars = Collections.unmodifiableList(copy);
    }

    public static Exemplars of(Collection<Exemplar> exemplars) {
        return new Exemplars(exemplars);
    }

    public static Exemplars of(Exemplar ... exemplars) {
        return new Exemplars(Arrays.asList(exemplars));
    }

    @Override
    public Iterator<Exemplar> iterator() {
        return this.exemplars.iterator();
    }

    public int size() {
        return this.exemplars.size();
    }

    public Exemplar get(int index) {
        return this.exemplars.get(index);
    }

    public Exemplar get(double lowerBound, double upperBound) {
        Exemplar result = null;
        for (Exemplar exemplar : this.exemplars) {
            double value = exemplar.getValue();
            if (!(value > lowerBound) || !(value <= upperBound)) continue;
            if (result == null) {
                result = exemplar;
                continue;
            }
            if (!result.hasTimestamp() || !exemplar.hasTimestamp() || exemplar.getTimestampMillis() <= result.getTimestampMillis()) continue;
            result = exemplar;
        }
        return result;
    }

    public Exemplar getLatest() {
        Exemplar latest = null;
        for (Exemplar candidate : this.exemplars) {
            if (candidate == null) continue;
            if (latest == null) {
                latest = candidate;
                continue;
            }
            if (!latest.hasTimestamp()) {
                latest = candidate;
                continue;
            }
            if (!candidate.hasTimestamp() || latest.getTimestampMillis() >= candidate.getTimestampMillis()) continue;
            latest = candidate;
        }
        return latest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ArrayList<Exemplar> exemplars = new ArrayList();

        private Builder() {
        }

        public Builder exemplar(Exemplar exemplar) {
            this.exemplars.add(exemplar);
            return this;
        }

        public Builder exemplars(Collection<Exemplar> exemplars) {
            this.exemplars.addAll(exemplars);
            return this;
        }

        public Exemplars build() {
            return Exemplars.of(this.exemplars);
        }
    }
}

