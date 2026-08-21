/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.TimestampStyle;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.MessageBuilderBaseDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.util.internal.DelegateFactory;

abstract class MessageBuilderBase<T> {
    private final Class<T> myClass;
    protected final MessageBuilderBaseDelegate delegate = DelegateFactory.createMessageBuilderBaseDelegate();

    protected MessageBuilderBase(Class<T> myClass) {
        this.myClass = myClass;
    }

    public T addComponents(HighLevelComponent ... components) {
        this.delegate.addComponents(components);
        return this.myClass.cast(this);
    }

    public T addActionRow(LowLevelComponent ... components) {
        this.delegate.addActionRow(components);
        return this.myClass.cast(this);
    }

    public T appendCode(String language, String code) {
        this.delegate.appendCode(language, code);
        return this.myClass.cast(this);
    }

    public T appendTimestamp(long epochSeconds) {
        this.appendTimestamp(epochSeconds, TimestampStyle.SHORT_DATE_TIME);
        return this.myClass.cast(this);
    }

    public T appendTimestamp(Instant instant) {
        this.appendTimestamp(instant.getEpochSecond(), TimestampStyle.SHORT_DATE_TIME);
        return this.myClass.cast(this);
    }

    public T appendTimestamp(long epochSeconds, TimestampStyle timestampStyle) {
        this.delegate.append(timestampStyle.getTimestampTag(epochSeconds));
        return this.myClass.cast(this);
    }

    public T appendTimestamp(Instant instant, TimestampStyle timestampStyle) {
        this.appendTimestamp(instant.getEpochSecond(), timestampStyle);
        return this.myClass.cast(this);
    }

    public T append(String message, MessageDecoration ... decorations) {
        this.delegate.append(message, decorations);
        return this.myClass.cast(this);
    }

    public T append(Mentionable entity) {
        this.delegate.append(entity);
        return this.myClass.cast(this);
    }

    public T append(Object object) {
        this.delegate.append(object);
        return this.myClass.cast(this);
    }

    public T appendNewLine() {
        this.delegate.appendNewLine();
        return this.myClass.cast(this);
    }

    public T setContent(String content) {
        this.delegate.setContent(content);
        return this.myClass.cast(this);
    }

    public T removeContent() {
        this.delegate.setContent(null);
        return this.myClass.cast(this);
    }

    public T setEmbed(EmbedBuilder embed) {
        this.delegate.removeAllEmbeds();
        this.delegate.addEmbed(embed);
        return this.myClass.cast(this);
    }

    public T setEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeAllEmbeds();
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this.myClass.cast(this);
    }

    public T setEmbeds(List<EmbedBuilder> embeds) {
        this.delegate.removeAllEmbeds();
        this.delegate.addEmbeds(embeds);
        return this.myClass.cast(this);
    }

    public T addEmbed(EmbedBuilder embed) {
        this.delegate.addEmbed(embed);
        return this.myClass.cast(this);
    }

    public T addAttachment(BufferedImage image, String fileName) {
        this.addAttachment(image, fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, fileName, description);
        return this.myClass.cast(this);
    }

    public T addAttachment(File file) {
        this.addAttachment(file, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(File file, String description) {
        this.delegate.addAttachment(file, description);
        return this.myClass.cast(this);
    }

    public T addAttachment(Icon icon) {
        this.addAttachment(icon, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(Icon icon, String description) {
        this.delegate.addAttachment(icon, description);
        return this.myClass.cast(this);
    }

    public T addAttachment(URL url) {
        this.addAttachment(url, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(URL url, String description) {
        this.delegate.addAttachment(url, description);
        return this.myClass.cast(this);
    }

    public T addAttachment(byte[] bytes, String fileName) {
        this.addAttachment(bytes, fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, fileName, description);
        return this.myClass.cast(this);
    }

    public T addAttachment(InputStream stream, String fileName) {
        this.addAttachment(stream, fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachment(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, fileName, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        this.addAttachment(image, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(File file) {
        this.addAttachmentAsSpoiler(file, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(File file, String description) {
        this.delegate.addAttachmentAsSpoiler(file, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(Icon icon) {
        this.addAttachmentAsSpoiler(icon, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(Icon icon, String description) {
        this.delegate.addAttachmentAsSpoiler(icon, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(URL url) {
        this.addAttachmentAsSpoiler(url, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(URL url, String description) {
        this.delegate.addAttachmentAsSpoiler(url, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        this.addAttachment(bytes, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(InputStream stream, String fileName) {
        this.addAttachment(stream, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    public T addAttachmentAsSpoiler(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }

    public T setAllowedMentions(AllowedMentions allowedMentions) {
        this.delegate.setAllowedMentions(allowedMentions);
        return this.myClass.cast(this);
    }

    public T removeAllComponents() {
        this.delegate.removeAllComponents();
        return this.myClass.cast(this);
    }

    public T addEmbeds(EmbedBuilder ... embeds) {
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this.myClass.cast(this);
    }

    public T addEmbeds(List<EmbedBuilder> embeds) {
        this.delegate.addEmbeds(embeds);
        return this.myClass.cast(this);
    }

    public T removeEmbed(EmbedBuilder embed) {
        this.delegate.removeEmbed(embed);
        return this.myClass.cast(this);
    }

    public T removeEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeEmbeds(embeds);
        return this.myClass.cast(this);
    }

    public T removeAllEmbeds() {
        this.delegate.removeAllEmbeds();
        return this.myClass.cast(this);
    }

    public T setNonce(String nonce) {
        this.delegate.setNonce(nonce);
        return this.myClass.cast(this);
    }

    public StringBuilder getStringBuilder() {
        return this.delegate.getStringBuilder();
    }
}

