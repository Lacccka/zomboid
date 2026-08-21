/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMOD_GUID;
import fmod.fmod.FMOD_STUDIO_PARAMETER_DESCRIPTION;
import java.util.ArrayList;

public final class FMOD_STUDIO_EVENT_DESCRIPTION {
    public final long address;
    public final String path;
    public final FMOD_GUID id;
    public final boolean hasSustainPoints;
    public final long length;
    public final ArrayList<FMOD_STUDIO_PARAMETER_DESCRIPTION> parameters = new ArrayList();

    public FMOD_STUDIO_EVENT_DESCRIPTION(long address, String path, FMOD_GUID id, boolean hasSustainPoints, long length) {
        this.address = address;
        this.path = path;
        this.id = id;
        this.hasSustainPoints = hasSustainPoints;
        this.length = length;
    }

    public boolean hasParameter(FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription) {
        return this.parameters.contains(parameterDescription);
    }
}

