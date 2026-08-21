/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

final class CKMSQuantiles {
    final Quantile[] quantiles;
    int n = 0;
    final LinkedList<Sample> samples = new LinkedList();
    private final int compressInterval = 128;
    private int insertsSinceLastCompress = 0;
    private final double[] buffer = new double[128];
    private int bufferPos = 0;

    public CKMSQuantiles(Quantile ... quantiles) {
        if (quantiles.length == 0) {
            throw new IllegalArgumentException("quantiles cannot be empty");
        }
        this.quantiles = quantiles;
    }

    public void insert(double value) {
        this.buffer[this.bufferPos++] = value;
        if (this.bufferPos == this.buffer.length) {
            this.flush();
        }
        if (++this.insertsSinceLastCompress == 128) {
            this.compress();
            this.insertsSinceLastCompress = 0;
        }
    }

    private void flush() {
        Arrays.sort(this.buffer, 0, this.bufferPos);
        this.insertBatch(this.buffer, this.bufferPos);
        this.bufferPos = 0;
    }

    void insertBatch(double[] sortedBuffer, int toIndex) {
        if (toIndex == 0) {
            return;
        }
        ListIterator<Sample> iterator2 = this.samples.listIterator();
        int i = 0;
        int r = 0;
        while (iterator2.hasNext() && i < toIndex) {
            Sample item = (Sample)iterator2.next();
            while (i < toIndex && !(sortedBuffer[i] > item.value)) {
                this.insertBefore(iterator2, sortedBuffer[i], r);
                ++r;
                ++i;
                ++this.n;
            }
            r += item.g;
        }
        while (i < toIndex) {
            this.samples.add(new Sample(sortedBuffer[i], 0));
            ++i;
            ++this.n;
        }
    }

    private void insertBefore(ListIterator<Sample> iterator2, double value, int r) {
        if (!iterator2.hasPrevious()) {
            this.samples.addFirst(new Sample(value, 0));
        } else {
            iterator2.previous();
            iterator2.add(new Sample(value, this.f(r) - 1));
            iterator2.next();
        }
    }

    public double get(double q) {
        this.flush();
        if (this.samples.isEmpty()) {
            return Double.NaN;
        }
        if (q == 0.0) {
            return this.samples.getFirst().value;
        }
        if (q == 1.0) {
            return this.samples.getLast().value;
        }
        int r = 0;
        int desiredRank = (int)Math.ceil(q * (double)this.n);
        int upperBound = desiredRank + this.f(desiredRank) / 2;
        ListIterator iterator2 = this.samples.listIterator();
        while (iterator2.hasNext()) {
            Sample sample = (Sample)iterator2.next();
            if (r + sample.g + sample.delta > upperBound) {
                iterator2.previous();
                if (iterator2.hasPrevious()) {
                    Sample result = (Sample)iterator2.previous();
                    return result.value;
                }
                return sample.value;
            }
            r += sample.g;
        }
        return this.samples.getLast().value;
    }

    int f(int r) {
        int minResult = Integer.MAX_VALUE;
        for (Quantile q : this.quantiles) {
            int result;
            if (q.quantile == 0.0 || q.quantile == 1.0 || (result = (double)r >= q.quantile * (double)this.n ? (int)(q.v * (double)r + 1.0E-11) : (int)(q.u * (double)(this.n - r) + 1.0E-11)) >= minResult) continue;
            minResult = result;
        }
        return Math.max(minResult, 1);
    }

    void compress() {
        if (this.samples.size() < 3) {
            return;
        }
        Iterator<Sample> descendingIterator = this.samples.descendingIterator();
        int r = this.n;
        Sample left = descendingIterator.next();
        r -= left.g;
        while (descendingIterator.hasNext()) {
            Sample right = left;
            left = descendingIterator.next();
            r -= left.g;
            if (left == this.samples.getFirst()) break;
            if (left.g + right.g + right.delta >= this.f(r)) continue;
            right.g += left.g;
            descendingIterator.remove();
            left = right;
        }
    }

    static class Quantile {
        final double quantile;
        final double epsilon;
        final double u;
        final double v;

        Quantile(double quantile, double epsilon) {
            if (quantile < 0.0 || quantile > 1.0) {
                throw new IllegalArgumentException("Quantile must be between 0 and 1");
            }
            if (epsilon < 0.0 || epsilon > 1.0) {
                throw new IllegalArgumentException("Epsilon must be between 0 and 1");
            }
            this.quantile = quantile;
            this.epsilon = epsilon;
            this.u = 2.0 * epsilon / (1.0 - quantile);
            this.v = 2.0 * epsilon / quantile;
        }

        public String toString() {
            return String.format("Quantile{q=%.3f, epsilon=%.3f}", this.quantile, this.epsilon);
        }
    }

    static class Sample {
        final double value;
        int g = 1;
        final int delta;

        Sample(double value, int delta) {
            this.value = value;
            this.delta = delta;
        }

        public String toString() {
            return String.format("Sample{val=%.3f, g=%d, delta=%d}", this.value, this.g, this.delta);
        }
    }
}

