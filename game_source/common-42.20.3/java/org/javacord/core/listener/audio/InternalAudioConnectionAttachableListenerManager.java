/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.audio;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioConnectionAttachableListener;
import org.javacord.api.listener.audio.AudioConnectionAttachableListenerManager;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalAudioConnectionAttachableListenerManager
extends AudioConnectionAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(AudioConnection.class, this.getId(), AudioSourceFinishedListener.class, listener);
    }

    @Override
    default public List<AudioSourceFinishedListener> getAudioSourceFinishedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(AudioConnection.class, this.getId(), AudioSourceFinishedListener.class);
    }

    @Override
    default public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addAudioConnectionAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(AudioConnectionAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(AudioConnection.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> void removeAudioConnectionAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(AudioConnectionAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(AudioConnection.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getAudioConnectionAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(AudioConnection.class, this.getId());
    }

    @Override
    default public <T extends AudioConnectionAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(AudioConnection.class, this.getId(), listenerClass, listener);
    }
}

