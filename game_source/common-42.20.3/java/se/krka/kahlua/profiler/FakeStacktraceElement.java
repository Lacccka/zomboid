/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.profiler;

import se.krka.kahlua.profiler.StacktraceElement;

public class FakeStacktraceElement
implements StacktraceElement {
    private final String name;
    private final String type;

    public FakeStacktraceElement(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return this.type;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FakeStacktraceElement)) {
            return false;
        }
        FakeStacktraceElement that = (FakeStacktraceElement)o;
        if (!this.name.equals(that.name)) {
            return false;
        }
        return this.type.equals(that.type);
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    public String toString() {
        return this.name;
    }
}

