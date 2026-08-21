/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

public final class FMOD_STUDIO_PARAMETER_ID {
    private final long nativePtr;

    public FMOD_STUDIO_PARAMETER_ID(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    public long address() {
        return this.nativePtr;
    }
}

