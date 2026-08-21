/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.javacord.api.audio.AudioSource;

public interface SeekableAudioSource
extends AudioSource {
    public long setPosition(long var1, TimeUnit var3);

    public Duration getPosition();

    public Duration getDuration();
}

