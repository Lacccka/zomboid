/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilderBase;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.util.DiscordRegexPattern;

public class MessageBuilder
extends MessageBuilderBase<MessageBuilder> {
    public MessageBuilder() {
        super(MessageBuilder.class);
    }

    public static MessageBuilder fromMessage(Message message) {
        MessageBuilder builder = new MessageBuilder();
        return builder.copy(message);
    }

    public MessageBuilder copy(Message message) {
        this.delegate.copy(message);
        return this;
    }

    public MessageBuilder setTts(boolean tts) {
        this.delegate.setTts(tts);
        return this;
    }

    public MessageBuilder addSticker(long stickerId) {
        this.delegate.addSticker(stickerId);
        return this;
    }

    public MessageBuilder addSticker(Sticker sticker) {
        this.delegate.addSticker(sticker.getId());
        return this;
    }

    public MessageBuilder addStickers(long ... stickerIds) {
        for (long stickerId : stickerIds) {
            this.delegate.addSticker(stickerId);
        }
        return this;
    }

    public MessageBuilder addStickers(Collection<Sticker> stickers) {
        this.delegate.addStickers(stickers.stream().map(DiscordEntity::getId).collect(Collectors.toList()));
        return this;
    }

    public MessageBuilder replyTo(Message message) {
        return this.replyTo(message, true);
    }

    public MessageBuilder replyTo(long messageId) {
        return this.replyTo(messageId, true);
    }

    public MessageBuilder replyTo(Message message, boolean assertReferenceExists) {
        return this.replyTo(message.getId(), assertReferenceExists);
    }

    public MessageBuilder replyTo(long messageId, boolean assertReferenceExists) {
        this.delegate.replyTo(messageId, assertReferenceExists);
        return this;
    }

    public CompletableFuture<Message> send(User user) {
        return this.delegate.send(user);
    }

    public CompletableFuture<Message> send(TextChannel channel) {
        return this.delegate.send(channel);
    }

    public CompletableFuture<Message> send(IncomingWebhook webhook) {
        return this.delegate.send(webhook);
    }

    public CompletableFuture<Message> send(Messageable messageable) {
        return this.delegate.send(messageable);
    }

    public CompletableFuture<Message> sendWithWebhook(DiscordApi api, long webhookId, String webhookToken) {
        return this.delegate.sendWithWebhook(api, Long.toUnsignedString(webhookId), webhookToken);
    }

    public CompletableFuture<Message> sendWithWebhook(DiscordApi api, String webhookId, String webhookToken) {
        return this.delegate.sendWithWebhook(api, webhookId, webhookToken);
    }

    public CompletableFuture<Message> sendWithWebhook(DiscordApi api, String webhookUrl) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.WEBHOOK_URL.matcher(webhookUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The webhook url has an invalid format");
        }
        return this.sendWithWebhook(api, matcher.group("id"), matcher.group("token"));
    }
}

