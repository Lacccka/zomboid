/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.util.List;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioTransformer;
import org.javacord.api.audio.BufferableAudioSource;
import org.javacord.api.audio.DownloadableAudioSource;
import org.javacord.api.audio.PauseableAudioSource;
import org.javacord.api.audio.SeekableAudioSource;
import org.javacord.api.listener.audio.AudioSourceAttachableListenerManager;
import org.javacord.api.util.Specializable;

public interface AudioSource
extends AudioSourceAttachableListenerManager,
Specializable<AudioSource> {
    public DiscordApi getApi();

    public void addTransformer(AudioTransformer var1);

    public boolean removeTransformer(AudioTransformer var1);

    public List<AudioTransformer> getTransformers();

    public void removeTransformers();

    public byte[] getNextFrame();

    public boolean hasNextFrame();

    public boolean hasFinished();

    default public void mute() {
        this.setMuted(true);
    }

    default public void unmute() {
        this.setMuted(false);
    }

    public void setMuted(boolean var1);

    public boolean isMuted();

    public AudioSource copy();

    default public Optional<PauseableAudioSource> asPauseableAudioSource() {
        return this.as(PauseableAudioSource.class);
    }

    default public Optional<DownloadableAudioSource> asDownloadableAudioSource() {
        return this.as(DownloadableAudioSource.class);
    }

    default public Optional<BufferableAudioSource> asBufferableAudioSource() {
        return this.as(BufferableAudioSource.class);
    }

    default public Optional<SeekableAudioSource> asSeekableAudioSource() {
        return this.as(SeekableAudioSource.class);
    }
}

