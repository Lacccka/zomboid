/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

public enum FMOD_STUDIO_PARAMETER_FLAGS {
    FMOD_STUDIO_PARAMETER_READONLY(1),
    FMOD_STUDIO_PARAMETER_AUTOMATIC(2),
    FMOD_STUDIO_PARAMETER_GLOBAL(4),
    FMOD_STUDIO_PARAMETER_DISCRETE(8);

    public final int bit;

    private FMOD_STUDIO_PARAMETER_FLAGS(int bit) {
        this.bit = bit;
    }
}

