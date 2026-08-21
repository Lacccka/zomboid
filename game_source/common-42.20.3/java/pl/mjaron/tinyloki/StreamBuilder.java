/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.util.Map;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.TinyLoki;

public class StreamBuilder {
    private final TinyLoki tinyLoki;
    private final Labels labels = Labels.of();

    public StreamBuilder(TinyLoki tinyLoki) {
        this.tinyLoki = tinyLoki;
    }

    public ILogStream open() {
        return this.tinyLoki.openStream(this.labels);
    }

    public Labels getLabels() {
        return this.labels;
    }

    public StreamBuilder l(String name, String value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, int value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, long value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, char value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, byte value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, short value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, float value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(String name, double value) {
        this.labels.l(name, value);
        return this;
    }

    public StreamBuilder l(Labels labels) {
        this.labels.l(labels);
        return this;
    }

    public StreamBuilder l(Map<String, String> labels) {
        this.labels.l(labels);
        return this;
    }

    public StreamBuilder critical() {
        this.labels.critical();
        return this;
    }

    public StreamBuilder fatal() {
        this.labels.fatal();
        return this;
    }

    public StreamBuilder warning() {
        this.labels.warning();
        return this;
    }

    public StreamBuilder info() {
        this.labels.info();
        return this;
    }

    public StreamBuilder debug() {
        this.labels.debug();
        return this;
    }

    public StreamBuilder verbose() {
        this.labels.verbose();
        return this;
    }

    public StreamBuilder trace() {
        this.labels.trace();
        return this;
    }

    public StreamBuilder unknown() {
        this.labels.unknown();
        return this;
    }
}

