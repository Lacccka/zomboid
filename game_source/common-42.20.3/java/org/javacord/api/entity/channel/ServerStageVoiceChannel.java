/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListenerManager;

public interface ServerStageVoiceChannel
extends ServerVoiceChannel,
ServerStageVoiceChannelAttachableListenerManager {
    @Override
    default public ChannelType getType() {
        return ChannelType.SERVER_STAGE_VOICE_CHANNEL;
    }

    public Optional<String> getTopic();

    default public Optional<ServerStageVoiceChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getChannelById(this.getId())).flatMap(Channel::asServerStageVoiceChannel);
    }

    @Override
    default public CompletableFuture<ServerStageVoiceChannel> getLatestInstance() {
        Optional<ServerStageVoiceChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture<ServerStageVoiceChannel> result = new CompletableFuture<ServerStageVoiceChannel>();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

