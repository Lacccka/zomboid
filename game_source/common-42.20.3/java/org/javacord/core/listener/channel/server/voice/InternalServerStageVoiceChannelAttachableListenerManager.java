/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel.server.voice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListenerManager;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelChangeTopicListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalServerStageVoiceChannelAttachableListenerManager
extends ServerStageVoiceChannelAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<ServerStageVoiceChannelChangeTopicListener> addServerStageVoiceChannelChangeTopicListener(ServerStageVoiceChannelChangeTopicListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerStageVoiceChannel.class, this.getId(), ServerStageVoiceChannelChangeTopicListener.class, listener);
    }

    @Override
    default public List<ServerStageVoiceChannelChangeTopicListener> getServerStageVoiceChannelChangeTopicListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerStageVoiceChannel.class, this.getId(), ServerStageVoiceChannelChangeTopicListener.class);
    }

    @Override
    default public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerStageVoiceChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerStageVoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(ServerStageVoiceChannel.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> void removeServerStageVoiceChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerStageVoiceChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerStageVoiceChannel.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerStageVoiceChannelAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerStageVoiceChannel.class, this.getId());
    }

    @Override
    default public <T extends ServerStageVoiceChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerStageVoiceChannel.class, this.getId(), listenerClass, listener);
    }
}

