/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.Map;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.StreamSet;
import pl.mjaron.tinyloki.TinyLoki;

public class StreamSetBuilder {
    private final TinyLoki tinyLoki;
    private final Labels labels = new Labels();

    public StreamSetBuilder(TinyLoki tinyLoki) {
        this.tinyLoki = tinyLoki;
    }

    public StreamSet open() {
        return this.tinyLoki.openStreamSet(this.labels);
    }

    public Labels getLabels() {
        return this.labels;
    }

    public StreamSetBuilder l(String name, String value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, int value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, long value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, char value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, byte value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, short value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, float value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(String name, double value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamSetBuilder l(Labels labels) {
        this.labels.l(labels);
        return this;
    }

    public StreamSetBuilder l(Map<String, String> labels) {
        this.labels.l(labels);
        return this;
    }
}

