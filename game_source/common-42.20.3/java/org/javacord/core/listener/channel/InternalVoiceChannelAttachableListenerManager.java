/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListenerManager;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalVoiceChannelAttachableListenerManager
extends VoiceChannelAttachableListenerManager,
InternalChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends VoiceChannelAttachableListener>> addVoiceChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(VoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(VoiceChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> void removeVoiceChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(VoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(VoiceChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getVoiceChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(VoiceChannel.class, this.getId());
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends VoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(VoiceChannel.class, this.getId(), listenerClass, listener);
    }
}

