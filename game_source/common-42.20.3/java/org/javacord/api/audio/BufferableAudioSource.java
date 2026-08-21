/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.javacord.api.audio.AudioSource;

public interface BufferableAudioSource
extends AudioSource {
    public void setBufferSize(long var1, TimeUnit var3);

    public Duration getBufferSize();

    public Duration getUsedBufferSize();
}

