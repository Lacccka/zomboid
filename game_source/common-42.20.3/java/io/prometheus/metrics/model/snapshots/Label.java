/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import java.util.Objects;

public final class Label
implements Comparable<Label> {
    private final String name;
    private final String value;

    public Label(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public int compareTo(Label other) {
        int nameCompare = this.name.compareTo(other.name);
        return nameCompare != 0 ? nameCompare : this.value.compareTo(other.value);
    }

    public String toString() {
        return "Label{name='" + this.name + '\'' + ", value='" + this.value + '\'' + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Label label = (Label)o;
        return Objects.equals(this.name, label.name) && Objects.equals(this.value, label.value);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }
}

