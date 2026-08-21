/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.invoke.MethodHandles;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Upcalls;
import org.lwjgl.system.libffi.FFICIF;

public abstract class Callback
implements Pointer,
NativeResource {
    private long address;

    protected Callback(Descriptor descriptor) {
        this.address = Upcalls.upcallCreate(descriptor, this);
    }

    protected Callback(long address) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        this.address = address;
    }

    @Override
    public long address() {
        return this.address;
    }

    @Override
    public void free() {
        Callback.free(this.address());
    }

    public static <T extends CallbackI> T get(long functionPointer) {
        return Upcalls.upcallGet(functionPointer);
    }

    public static <T extends CallbackI> @Nullable T getSafe(long functionPointer) {
        return functionPointer == 0L ? null : (T)Callback.get(functionPointer);
    }

    public static void free(long functionPointer) {
        Upcalls.upcallFree(functionPointer);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Callback)) {
            return false;
        }
        Callback that = (Callback)o;
        return this.address == that.address();
    }

    public int hashCode() {
        return (int)(this.address ^ this.address >>> 32);
    }

    public String toString() {
        return String.format("%s pointer [0x%X]", this.getClass().getSimpleName(), this.address);
    }

    public static final class Descriptor {
        final MethodHandles.Lookup lookup;
        final FFICIF cif;

        public Descriptor(MethodHandles.Lookup lookup, FFICIF cif) {
            this.lookup = lookup;
            this.cif = cif;
        }
    }
}

