/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.audio;

import org.javacord.api.event.audio.AudioSourceFinishedEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioConnectionAttachableListener;
import org.javacord.api.listener.audio.AudioSourceAttachableListener;

@FunctionalInterface
public interface AudioSourceFinishedListener
extends AudioSourceAttachableListener,
AudioConnectionAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onAudioSourceFinished(AudioSourceFinishedEvent var1);
}

