/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.audio;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioSourceAttachableListener;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.util.event.ListenerManager;

public interface AudioSourceAttachableListenerManager {
    public ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener var1);

    public List<AudioSourceFinishedListener> getAudioSourceFinishedListeners();

    public <T extends AudioSourceAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addAudioSourceAttachableListener(T var1);

    public <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeAudioSourceAttachableListener(T var1);

    public <T extends AudioSourceAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getAudioSourceAttachableListeners();

    public <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

