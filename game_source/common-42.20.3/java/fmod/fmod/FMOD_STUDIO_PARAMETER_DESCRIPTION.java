/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMOD_STUDIO_PARAMETER_FLAGS;
import fmod.fmod.FMOD_STUDIO_PARAMETER_ID;

public final class FMOD_STUDIO_PARAMETER_DESCRIPTION {
    public final String name;
    public final FMOD_STUDIO_PARAMETER_ID id;
    public final int flags;
    public final int globalIndex;

    public FMOD_STUDIO_PARAMETER_DESCRIPTION(String name, FMOD_STUDIO_PARAMETER_ID id, int flags, int globalIndex) {
        this.name = name;
        this.id = id;
        this.flags = flags;
        this.globalIndex = globalIndex;
    }

    public boolean isGlobal() {
        return (this.flags & FMOD_STUDIO_PARAMETER_FLAGS.FMOD_STUDIO_PARAMETER_GLOBAL.bit) != 0;
    }
}

