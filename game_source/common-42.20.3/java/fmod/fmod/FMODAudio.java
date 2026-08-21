/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.Audio;
import zombie.UsedFromLua;
import zombie.audio.BaseSoundEmitter;

@UsedFromLua
public class FMODAudio
implements Audio {
    public BaseSoundEmitter emitter;

    public FMODAudio(BaseSoundEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public boolean isPlaying() {
        return !this.emitter.isEmpty();
    }

    @Override
    public void setVolume(float volume) {
        this.emitter.setVolumeAll(volume);
    }

    @Override
    public void start() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
        this.emitter.stopAll();
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public String getName() {
        return null;
    }
}

