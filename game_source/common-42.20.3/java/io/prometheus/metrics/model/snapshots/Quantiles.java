/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Quantile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Quantiles
implements Iterable<Quantile> {
    private final List<Quantile> quantiles;
    public static final Quantiles EMPTY = new Quantiles(Collections.emptyList());

    private Quantiles(List<Quantile> quantiles) {
        quantiles = new ArrayList<Quantile>(quantiles);
        quantiles.sort(Comparator.comparing(Quantile::getQuantile));
        this.quantiles = Collections.unmodifiableList(quantiles);
        this.validate();
    }

    private void validate() {
        for (int i = 0; i < this.quantiles.size() - 1; ++i) {
            if (this.quantiles.get(i).getQuantile() != this.quantiles.get(i + 1).getQuantile()) continue;
            throw new IllegalArgumentException("Duplicate " + this.quantiles.get(i).getQuantile() + " quantile.");
        }
    }

    public static Quantiles of(List<Quantile> quantiles) {
        return new Quantiles(quantiles);
    }

    public static Quantiles of(Quantile ... quantiles) {
        return Quantiles.of(Arrays.asList(quantiles));
    }

    public int size() {
        return this.quantiles.size();
    }

    public Quantile get(int i) {
        return this.quantiles.get(i);
    }

    @Override
    public Iterator<Quantile> iterator() {
        return this.quantiles.iterator();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Quantile> quantiles = new ArrayList<Quantile>();

        private Builder() {
        }

        public Builder quantile(Quantile quantile) {
            this.quantiles.add(quantile);
            return this;
        }

        public Builder quantile(double quantile, double value) {
            this.quantiles.add(new Quantile(quantile, value));
            return this;
        }

        public Quantiles build() {
            return new Quantiles(this.quantiles);
        }
    }
}

