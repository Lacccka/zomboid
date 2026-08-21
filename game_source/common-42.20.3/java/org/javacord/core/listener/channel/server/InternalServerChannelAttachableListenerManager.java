/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.ServerChannelChangeNameListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.ServerChannelChangePositionListener;
import org.javacord.api.listener.channel.server.ServerChannelDeleteListener;
import org.javacord.api.listener.server.VoiceStateUpdateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalServerChannelAttachableListenerManager
extends ServerChannelAttachableListenerManager,
InternalChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<VoiceStateUpdateListener> addVoiceStateUpdateListener(VoiceStateUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), VoiceStateUpdateListener.class, listener);
    }

    @Override
    default public List<VoiceStateUpdateListener> getVoiceStateUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId(), VoiceStateUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangePositionListener> addServerChannelChangePositionListener(ServerChannelChangePositionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), ServerChannelChangePositionListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId(), ServerChannelChangePositionListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelDeleteListener> addServerChannelDeleteListener(ServerChannelDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), ServerChannelDeleteListener.class, listener);
    }

    @Override
    default public List<ServerChannelDeleteListener> getServerChannelDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId(), ServerChannelDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeNameListener> addServerChannelChangeNameListener(ServerChannelChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), ServerChannelChangeNameListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId(), ServerChannelChangeNameListener.class);
    }

    @Override
    default public <T extends ServerChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerChannelAttachableListener>> addServerChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(ServerChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerChannelAttachableListener & ObjectAttachableListener> void removeServerChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends ServerChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerChannel.class, this.getId());
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends ServerChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerChannel.class, this.getId(), listenerClass, listener);
    }
}

