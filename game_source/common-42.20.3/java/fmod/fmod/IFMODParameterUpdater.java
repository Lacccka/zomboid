/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import java.util.BitSet;
import zombie.audio.FMODParameterList;
import zombie.audio.GameSoundClip;

public interface IFMODParameterUpdater {
    public FMODParameterList getFMODParameters();

    public void startEvent(long var1, GameSoundClip var3, boolean var4, BitSet var5);

    public void updateEvent(long var1, GameSoundClip var3);

    public void stopEvent(long var1, GameSoundClip var3, boolean var4, BitSet var5);
}

