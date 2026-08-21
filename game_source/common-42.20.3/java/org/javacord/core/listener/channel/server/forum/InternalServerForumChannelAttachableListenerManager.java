/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server.forum;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.forum.ServerForumChannelAttachableListener;
import org.javacord.api.listener.channel.server.forum.ServerForumChannelAttachableListenerManager;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalServerForumChannelAttachableListenerManager
extends ServerForumChannelAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerForumChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerForumChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(ServerForumChannel.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> void removeServerForumChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerForumChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerForumChannel.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerForumChannelAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerForumChannel.class, this.getId());
    }

    @Override
    default public <T extends ServerForumChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerForumChannel.class, this.getId(), listenerClass, listener);
    }
}

