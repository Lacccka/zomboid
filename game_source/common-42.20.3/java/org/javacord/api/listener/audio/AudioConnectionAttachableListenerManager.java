/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.audio;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioConnectionAttachableListener;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.util.event.ListenerManager;

public interface AudioConnectionAttachableListenerManager {
    public ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener var1);

    public List<AudioSourceFinishedListener> getAudioSourceFinishedListeners();

    public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addAudioConnectionAttachableListener(T var1);

    public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> void removeAudioConnectionAttachableListener(T var1);

    public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getAudioConnectionAttachableListeners();

    public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

