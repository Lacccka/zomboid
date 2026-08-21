/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemorySegment;
import org.lwjgl.system.ffm.Struct;
import org.lwjgl.system.ffm.Union;

public sealed interface Group<L extends GroupLayout, T extends Group<L, T>>
permits Struct, Union {
    public L layout();

    public long address();

    public T copyFrom(T var1);

    public T clear();

    public T get(MemorySegment var1);

    public T get(MemorySegment var1, long var2);

    public T getAtIndex(MemorySegment var1, long var2);

    public T set(MemorySegment var1);

    public T set(MemorySegment var1, long var2);

    public T setAtIndex(MemorySegment var1, long var2);

    default public long sizeof() {
        return this.layout().byteSize();
    }

    default public long alignof() {
        return this.layout().byteAlignment();
    }

    default public MemorySegment asSegment() {
        return MemorySegment.ofAddress(this.address()).reinterpret(this.layout().byteSize());
    }
}

