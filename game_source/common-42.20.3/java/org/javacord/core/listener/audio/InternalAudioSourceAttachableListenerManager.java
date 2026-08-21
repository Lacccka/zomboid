/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.audio;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioSourceAttachableListener;
import org.javacord.api.listener.audio.AudioSourceAttachableListenerManager;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalAudioSourceAttachableListenerManager
extends AudioSourceAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(AudioSource.class, this.getId(), AudioSourceFinishedListener.class, listener);
    }

    @Override
    default public List<AudioSourceFinishedListener> getAudioSourceFinishedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(AudioSource.class, this.getId(), AudioSourceFinishedListener.class);
    }

    @Override
    default public <T extends AudioSourceAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addAudioSourceAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(AudioSourceAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(AudioSource.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeAudioSourceAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(AudioSourceAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(AudioSource.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends AudioSourceAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getAudioSourceAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(AudioSource.class, this.getId());
    }

    @Override
    default public <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(AudioSource.class, this.getId(), listenerClass, listener);
    }
}

