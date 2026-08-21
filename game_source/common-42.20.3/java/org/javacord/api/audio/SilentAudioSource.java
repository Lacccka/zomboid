/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;
import org.javacord.api.audio.PauseableAudioSource;
import org.javacord.api.audio.SeekableAudioSource;

public class SilentAudioSource
extends AudioSourceBase
implements PauseableAudioSource,
SeekableAudioSource {
    public static final byte[] SILENCE_FRAME = new byte[]{-8, -1, -2};
    private final long duration;
    private final AtomicLong position;
    private volatile boolean paused = false;

    public SilentAudioSource(DiscordApi api, long duration, TimeUnit unit) {
        super(api);
        this.duration = unit.toMillis(duration) / 20L;
        this.position = new AtomicLong(this.duration);
    }

    public SilentAudioSource(SilentAudioSource toCopy) {
        this(toCopy.getApi(), toCopy.duration * 20L, TimeUnit.MILLISECONDS);
    }

    @Override
    public byte[] getNextFrame() {
        return this.applyTransformers(null);
    }

    @Override
    public boolean hasNextFrame() {
        if (this.paused) {
            return false;
        }
        this.position.getAndIncrement();
        return false;
    }

    @Override
    public boolean hasFinished() {
        return this.position.get() >= this.duration;
    }

    @Override
    public boolean isMuted() {
        return true;
    }

    @Override
    public AudioSource copy() {
        return new SilentAudioSource(this);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public long setPosition(long position, TimeUnit unit) {
        long newPosition = unit.toMillis(position) / 20L;
        if (newPosition >= this.duration) {
            newPosition = this.duration;
        }
        this.position.set(newPosition);
        return unit.convert(newPosition * 20L, TimeUnit.MILLISECONDS);
    }

    @Override
    public Duration getPosition() {
        return Duration.ofMillis(this.position.get() * 20L);
    }

    @Override
    public Duration getDuration() {
        return Duration.ofMillis(this.duration * 20L);
    }
}

