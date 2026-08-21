/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.system.Callback;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Upcalls;

public interface CallbackI
extends Pointer {
    public Callback.Descriptor getDescriptor();

    @Override
    default public long address() {
        return Upcalls.upcallCreate(this.getDescriptor(), this);
    }

    public void callback(long var1, long var3);
}

