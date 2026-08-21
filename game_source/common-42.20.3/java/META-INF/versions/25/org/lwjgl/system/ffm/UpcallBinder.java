/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.Binder;

public interface UpcallBinder<T>
extends Binder<T> {
    public FunctionDescriptor descriptor();

    public MethodHandle handle();

    public @Nullable MemoryLayout stack();

    default public MemorySegment allocate(Arena arena, T upcall) {
        return this.allocate(arena, upcall, BCUtil.EMPTY_OPTIONS);
    }

    default public MemorySegment allocate(Arena arena, T upcall, Linker.Option ... options) {
        Objects.requireNonNull(upcall);
        MethodHandle handle = this.handle().bindTo(upcall);
        MemoryLayout stack = this.stack();
        if (stack != null) {
            handle = handle.bindTo(arena.allocate(stack));
        }
        return Linker.nativeLinker().upcallStub(handle, this.descriptor(), arena, options);
    }
}

