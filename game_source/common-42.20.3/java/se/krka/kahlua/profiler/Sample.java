/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.profiler;

import java.util.List;
import se.krka.kahlua.profiler.StacktraceElement;

public class Sample {
    private final List<StacktraceElement> list;
    private final long time;

    public Sample(List<StacktraceElement> list, long time) {
        this.list = list;
        this.time = time;
    }

    public List<StacktraceElement> getList() {
        return this.list;
    }

    public long getTime() {
        return this.time;
    }
}

