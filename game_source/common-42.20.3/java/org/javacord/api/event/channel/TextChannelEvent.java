/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel;

import java.util.Optional;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.channel.ChannelEvent;

public interface TextChannelEvent
extends ChannelEvent {
    @Override
    public TextChannel getChannel();

    default public Optional<ServerTextChannel> getServerTextChannel() {
        return this.getChannel().asServerTextChannel();
    }

    default public Optional<ServerVoiceChannel> getServerVoiceChannel() {
        return this.getChannel().asServerVoiceChannel();
    }

    default public Optional<ServerThreadChannel> getServerThreadChannel() {
        return this.getChannel().asServerThreadChannel();
    }

    default public Optional<PrivateChannel> getPrivateChannel() {
        return this.getChannel().asPrivateChannel();
    }
}

