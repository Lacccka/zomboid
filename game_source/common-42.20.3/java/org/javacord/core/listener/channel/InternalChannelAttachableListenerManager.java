/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListenerManager;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalChannelAttachableListenerManager
extends ChannelAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public <T extends ChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Channel.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(Channel.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends ChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getChannelAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Channel.class, this.getId());
    }

    @Override
    default public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Channel.class, this.getId(), listenerClass, listener);
    }
}

