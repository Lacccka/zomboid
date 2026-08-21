/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import org.javacord.api.audio.AudioSource;

public interface PauseableAudioSource
extends AudioSource {
    default public void pause() {
        this.setPaused(true);
    }

    default public void resume() {
        this.setPaused(false);
    }

    public void setPaused(boolean var1);

    public boolean isPaused();
}

