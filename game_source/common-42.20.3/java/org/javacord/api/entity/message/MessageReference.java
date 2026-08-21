/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

public interface MessageReference {
    public DiscordApi getApi();

    public Optional<Long> getServerId();

    public long getChannelId();

    public Optional<Long> getMessageId();

    public Optional<Message> getMessage();

    default public Optional<Server> getServer() {
        return this.getServerId().flatMap(id -> this.getApi().getServerById((long)id));
    }

    default public Optional<TextChannel> getChannel() {
        return this.getApi().getTextChannelById(this.getChannelId());
    }

    default public Optional<CompletableFuture<Message>> requestMessage() {
        Optional<Message> optionalMessage = this.getMessage();
        if (optionalMessage.isPresent()) {
            return optionalMessage.map(CompletableFuture::completedFuture);
        }
        return this.getMessageId().flatMap(messageId -> this.getChannel().map(channel -> channel.getMessageById((long)messageId)));
    }
}

