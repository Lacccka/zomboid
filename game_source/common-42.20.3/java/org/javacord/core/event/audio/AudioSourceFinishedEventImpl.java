/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.audio;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.event.audio.AudioSourceFinishedEvent;
import org.javacord.core.event.audio.AudioSourceEventImpl;

public class AudioSourceFinishedEventImpl
extends AudioSourceEventImpl
implements AudioSourceFinishedEvent {
    public AudioSourceFinishedEventImpl(AudioSource source2, AudioConnection connection) {
        super(source2, connection);
    }
}

