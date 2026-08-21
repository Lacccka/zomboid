/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import se.krka.kahlua.vm.Coroutine;

public final class UpValue {
    private Coroutine coroutine;
    private final int index;
    private Object value;

    public UpValue(Coroutine coroutine, int index) {
        this.coroutine = coroutine;
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public final Object getValue() {
        if (this.coroutine == null) {
            return this.value;
        }
        return this.coroutine.objectStack[this.index];
    }

    public final void setValue(Object object) {
        if (this.coroutine == null) {
            this.value = object;
            return;
        }
        this.coroutine.objectStack[this.index] = object;
    }

    public void close() {
        this.value = this.coroutine.objectStack[this.index];
        this.coroutine = null;
    }
}

