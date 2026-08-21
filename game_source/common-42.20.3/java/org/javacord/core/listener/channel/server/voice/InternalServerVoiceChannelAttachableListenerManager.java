/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server.voice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeBitrateListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeNsfwListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelChangeUserLimitListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalVoiceChannelAttachableListenerManager;
import org.javacord.core.listener.channel.server.InternalServerChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalServerVoiceChannelAttachableListenerManager
extends ServerVoiceChannelAttachableListenerManager,
InternalVoiceChannelAttachableListenerManager,
InternalServerChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<ServerVoiceChannelChangeBitrateListener> addServerVoiceChannelChangeBitrateListener(ServerVoiceChannelChangeBitrateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeBitrateListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeBitrateListener> getServerVoiceChannelChangeBitrateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeBitrateListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelChangeUserLimitListener> addServerVoiceChannelChangeUserLimitListener(ServerVoiceChannelChangeUserLimitListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeUserLimitListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeUserLimitListener> getServerVoiceChannelChangeUserLimitListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeUserLimitListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelChangeNsfwListener> addServerVoiceChannelChangeNsfwListener(ServerVoiceChannelChangeNsfwListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeNsfwListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelChangeNsfwListener> getServerVoiceChannelChangeNsfwListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelChangeNsfwListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelMemberJoinListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId(), ServerVoiceChannelMemberJoinListener.class);
    }

    @Override
    default public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends ServerVoiceChannelAttachableListener>> addServerVoiceChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerVoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            if (VoiceChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addVoiceChannelAttachableListener((VoiceChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((VoiceChannelAttachableListener)listener))))).stream();
            }
            if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(ServerVoiceChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> void removeServerVoiceChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerVoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else if (VoiceChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeVoiceChannelAttachableListener((VoiceChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((VoiceChannelAttachableListener)listener)))));
            } else if (ServerChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerChannelAttachableListener((ServerChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerVoiceChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerVoiceChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerVoiceChannel.class, this.getId());
        this.getServerChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getVoiceChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends ServerVoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerVoiceChannel.class, this.getId(), listenerClass, listener);
    }
}

