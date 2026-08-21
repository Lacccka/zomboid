/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import se.krka.kahlua.vm.JavaFunction;

public interface KahluaTableIterator
extends JavaFunction {
    public boolean advance();

    public Object getKey();

    public Object getValue();
}

