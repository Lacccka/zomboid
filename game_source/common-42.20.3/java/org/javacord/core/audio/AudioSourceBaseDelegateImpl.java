/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.audio;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioTransformer;
import org.javacord.api.audio.internal.AudioSourceBaseDelegate;
import org.javacord.core.listener.audio.InternalAudioSourceAttachableListenerManager;

public class AudioSourceBaseDelegateImpl
implements AudioSourceBaseDelegate,
InternalAudioSourceAttachableListenerManager {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final long id;
    private final DiscordApi api;

    public AudioSourceBaseDelegateImpl(DiscordApi api) {
        this.api = api;
        this.id = idCounter.getAndIncrement();
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void addTransformer(AudioTransformer transformer) {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public boolean removeTransformer(AudioTransformer transformer) {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public List<AudioTransformer> getTransformers() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public void removeTransformers() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public byte[] getNextFrame() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public boolean hasNextFrame() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public boolean hasFinished() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public boolean isMuted() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public void setMuted(boolean muted) {
        throw new UnsupportedOperationException("Not supported in delegate");
    }

    @Override
    public AudioSource copy() {
        throw new UnsupportedOperationException("Not supported in delegate");
    }
}

