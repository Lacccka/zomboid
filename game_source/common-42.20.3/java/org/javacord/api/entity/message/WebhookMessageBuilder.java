/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.WebhookMessageBuilderDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.internal.DelegateFactory;

public class WebhookMessageBuilder {
    protected final WebhookMessageBuilderDelegate delegate = DelegateFactory.createWebhookMessageBuilderDelegate();

    public static WebhookMessageBuilder fromMessage(Message message) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder().setDisplayAvatar(message.getAuthor().getAvatar()).setDisplayName(message.getAuthor().getDisplayName());
        builder.getStringBuilder().append(message.getContent());
        if (!message.getEmbeds().isEmpty()) {
            message.getEmbeds().forEach(embed -> builder.addEmbed(embed.toBuilder()));
        }
        for (MessageAttachment attachment : message.getAttachments()) {
            builder.addAttachment(attachment.getUrl(), (String)attachment.getDescription().orElse(null));
        }
        return builder;
    }

    public WebhookMessageBuilder appendCode(String language, String code) {
        this.delegate.appendCode(language, code);
        return this;
    }

    public WebhookMessageBuilder append(String message, MessageDecoration ... decorations) {
        this.delegate.append(message, decorations);
        return this;
    }

    public WebhookMessageBuilder append(Mentionable entity) {
        this.delegate.append(entity);
        return this;
    }

    public WebhookMessageBuilder append(Object object) {
        this.delegate.append(object);
        return this;
    }

    public WebhookMessageBuilder appendNewLine() {
        this.delegate.appendNewLine();
        return this;
    }

    public WebhookMessageBuilder addComponents(HighLevelComponent ... components) {
        this.delegate.addComponents(components);
        return this;
    }

    public WebhookMessageBuilder removeAllComponents() {
        this.delegate.removeAllComponents();
        return this;
    }

    public WebhookMessageBuilder removeComponent(int index) {
        this.delegate.removeComponent(index);
        return this;
    }

    public WebhookMessageBuilder removeComponent(HighLevelComponent builder) {
        this.delegate.removeComponent(builder);
        return this;
    }

    public WebhookMessageBuilder setContent(String content) {
        this.delegate.setContent(content);
        return this;
    }

    public WebhookMessageBuilder addEmbed(EmbedBuilder embed) {
        this.delegate.addEmbed(embed);
        return this;
    }

    public WebhookMessageBuilder addEmbeds(EmbedBuilder ... embeds) {
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this;
    }

    public WebhookMessageBuilder removeEmbed(EmbedBuilder embed) {
        this.delegate.removeEmbed(embed);
        return this;
    }

    public WebhookMessageBuilder removeEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeEmbeds(embeds);
        return this;
    }

    public WebhookMessageBuilder removeAllEmbeds() {
        this.delegate.removeAllEmbeds();
        return this;
    }

    public WebhookMessageBuilder setTts(boolean tts) {
        this.delegate.setTts(tts);
        return this;
    }

    public WebhookMessageBuilder addAttachment(BufferedImage image, String fileName) {
        this.addAttachment(image, fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, fileName, description);
        return this;
    }

    public WebhookMessageBuilder addAttachment(File file) {
        this.addAttachment(file, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(File file, String description) {
        this.delegate.addAttachment(file, description);
        return this;
    }

    public WebhookMessageBuilder addAttachment(Icon icon) {
        this.addAttachment(icon, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(Icon icon, String description) {
        this.delegate.addAttachment(icon, description);
        return this;
    }

    public WebhookMessageBuilder addAttachment(URL url) {
        this.addAttachment(url, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(URL url, String description) {
        this.delegate.addAttachment(url, description);
        return this;
    }

    public WebhookMessageBuilder addAttachment(byte[] bytes, String fileName) {
        this.addAttachment(bytes, fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, fileName, description);
        return this;
    }

    public WebhookMessageBuilder addAttachment(InputStream stream, String fileName) {
        this.addAttachment(stream, fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachment(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, fileName, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        this.addAttachment(image, "SPOILER_" + fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, "SPOILER_" + fileName, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(File file) {
        this.addAttachmentAsSpoiler(file, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(File file, String description) {
        this.delegate.addAttachmentAsSpoiler(file, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(Icon icon) {
        this.addAttachmentAsSpoiler(icon, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(Icon icon, String description) {
        this.delegate.addAttachmentAsSpoiler(icon, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(URL url) {
        this.addAttachmentAsSpoiler(url, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(URL url, String description) {
        this.delegate.addAttachmentAsSpoiler(url, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        this.addAttachment(bytes, "SPOILER_" + fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, "SPOILER_" + fileName, description);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(InputStream stream, String fileName) {
        this.addAttachment(stream, "SPOILER_" + fileName, null);
        return this;
    }

    public WebhookMessageBuilder addAttachmentAsSpoiler(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, "SPOILER_" + fileName, description);
        return this;
    }

    public WebhookMessageBuilder setAllowedMentions(AllowedMentions allowedMentions) {
        this.delegate.setAllowedMentions(allowedMentions);
        return this;
    }

    public WebhookMessageBuilder setDisplayName(String displayName) {
        this.delegate.setDisplayName(displayName);
        return this;
    }

    public WebhookMessageBuilder setDisplayAvatar(URL avatarUrl) {
        this.delegate.setDisplayAvatar(avatarUrl);
        return this;
    }

    public WebhookMessageBuilder setDisplayAvatar(Icon avatar) {
        this.delegate.setDisplayAvatar(avatar);
        return this;
    }

    public WebhookMessageBuilder setDisplayAuthor(MessageAuthor author) {
        this.delegate.setDisplayAuthor(author);
        return this;
    }

    public WebhookMessageBuilder setDisplayAuthor(User author) {
        this.delegate.setDisplayAuthor(author);
        return this;
    }

    public StringBuilder getStringBuilder() {
        return this.delegate.getStringBuilder();
    }

    public CompletableFuture<Message> send(IncomingWebhook webhook) {
        return this.delegate.send(webhook);
    }

    public CompletableFuture<Message> send(DiscordApi api, long webhookId, String webhookToken) {
        return this.delegate.send(api, Long.toUnsignedString(webhookId), webhookToken);
    }

    public CompletableFuture<Message> send(DiscordApi api, String webhookId, String webhookToken) {
        return this.delegate.send(api, webhookId, webhookToken);
    }

    public CompletableFuture<Message> send(DiscordApi api, String webhookUrl) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.WEBHOOK_URL.matcher(webhookUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The webhook url has an invalid format");
        }
        return this.send(api, matcher.group("id"), matcher.group("token"));
    }

    public CompletableFuture<Void> sendSilently(IncomingWebhook webhook) {
        return this.delegate.sendSilently(webhook);
    }

    public CompletableFuture<Void> sendSilently(DiscordApi api, long webhookId, String webhookToken) {
        return this.delegate.sendSilently(api, Long.toUnsignedString(webhookId), webhookToken);
    }

    public CompletableFuture<Void> sendSilently(DiscordApi api, String webhookId, String webhookToken) {
        return this.delegate.sendSilently(api, webhookId, webhookToken);
    }

    public CompletableFuture<Void> sendSilently(DiscordApi api, String webhookUrl) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.WEBHOOK_URL.matcher(webhookUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The webhook url has an invalid format");
        }
        return this.sendSilently(api, matcher.group("id"), matcher.group("token"));
    }
}

