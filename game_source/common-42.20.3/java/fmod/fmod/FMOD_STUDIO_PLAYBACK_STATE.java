/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

public enum FMOD_STUDIO_PLAYBACK_STATE {
    FMOD_STUDIO_PLAYBACK_PLAYING(0),
    FMOD_STUDIO_PLAYBACK_SUSTAINING(1),
    FMOD_STUDIO_PLAYBACK_STOPPED(2),
    FMOD_STUDIO_PLAYBACK_STARTING(3),
    FMOD_STUDIO_PLAYBACK_STOPPING(4),
    FMOD_STUDIO_PLAYBACK_STATE(5);

    public final int index;

    private FMOD_STUDIO_PLAYBACK_STATE(int index) {
        this.index = index;
    }
}

