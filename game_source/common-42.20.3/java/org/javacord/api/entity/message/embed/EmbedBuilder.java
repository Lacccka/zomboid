/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.internal.DelegateFactory;

public class EmbedBuilder {
    private final EmbedBuilderDelegate delegate = DelegateFactory.createEmbedBuilderDelegate();

    public EmbedBuilderDelegate getDelegate() {
        return this.delegate;
    }

    public EmbedBuilder setTitle(String title) {
        this.delegate.setTitle(title);
        return this;
    }

    public EmbedBuilder setDescription(String description) {
        this.delegate.setDescription(description);
        return this;
    }

    public EmbedBuilder setUrl(String url) {
        this.delegate.setUrl(url);
        return this;
    }

    public EmbedBuilder setTimestampToNow() {
        this.delegate.setTimestampToNow();
        return this;
    }

    public EmbedBuilder setTimestamp(Instant timestamp) {
        this.delegate.setTimestamp(timestamp);
        return this;
    }

    public EmbedBuilder setColor(Color color) {
        this.delegate.setColor(color);
        return this;
    }

    public EmbedBuilder setFooter(String text) {
        this.delegate.setFooter(text);
        return this;
    }

    public EmbedBuilder setFooter(String text, String iconUrl) {
        this.delegate.setFooter(text, iconUrl);
        return this;
    }

    public EmbedBuilder setFooter(String text, Icon icon) {
        this.delegate.setFooter(text, icon);
        return this;
    }

    public EmbedBuilder setFooter(String text, File icon) {
        this.delegate.setFooter(text, icon);
        return this;
    }

    public EmbedBuilder setFooter(String text, InputStream icon) {
        this.delegate.setFooter(text, icon);
        return this;
    }

    public EmbedBuilder setFooter(String text, InputStream icon, String fileType) {
        this.delegate.setFooter(text, icon, fileType);
        return this;
    }

    public EmbedBuilder setFooter(String text, byte[] icon) {
        this.delegate.setFooter(text, icon);
        return this;
    }

    public EmbedBuilder setFooter(String text, byte[] icon, String fileType) {
        this.delegate.setFooter(text, icon, fileType);
        return this;
    }

    public EmbedBuilder setFooter(String text, BufferedImage icon) {
        this.delegate.setFooter(text, icon);
        return this;
    }

    public EmbedBuilder setFooter(String text, BufferedImage icon, String fileType) {
        this.delegate.setFooter(text, icon, fileType);
        return this;
    }

    public EmbedBuilder setImage(String url) {
        this.delegate.setImage(url);
        return this;
    }

    public EmbedBuilder setImage(Icon image) {
        this.delegate.setImage(image);
        return this;
    }

    public EmbedBuilder setImage(File image) {
        this.delegate.setImage(image);
        return this;
    }

    public EmbedBuilder setImage(InputStream image) {
        this.delegate.setImage(image);
        return this;
    }

    public EmbedBuilder setImage(InputStream image, String fileType) {
        this.delegate.setImage(image, fileType);
        return this;
    }

    public EmbedBuilder setImage(byte[] image) {
        this.delegate.setImage(image);
        return this;
    }

    public EmbedBuilder setImage(byte[] image, String fileType) {
        this.delegate.setImage(image, fileType);
        return this;
    }

    public EmbedBuilder setImage(BufferedImage image) {
        this.delegate.setImage(image);
        return this;
    }

    public EmbedBuilder setImage(BufferedImage image, String fileType) {
        this.delegate.setImage(image, fileType);
        return this;
    }

    public EmbedBuilder setAuthor(MessageAuthor author) {
        this.delegate.setAuthor(author);
        return this;
    }

    public EmbedBuilder setAuthor(User author) {
        this.delegate.setAuthor(author);
        return this;
    }

    public EmbedBuilder setAuthor(String name) {
        this.delegate.setAuthor(name);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, String iconUrl) {
        this.delegate.setAuthor(name, url, iconUrl);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, Icon icon) {
        this.delegate.setAuthor(name, url, icon);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, File icon) {
        this.delegate.setAuthor(name, url, icon);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, InputStream icon) {
        this.delegate.setAuthor(name, url, icon);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, InputStream icon, String fileType) {
        this.delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, byte[] icon) {
        this.delegate.setAuthor(name, url, icon);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, byte[] icon, String fileType) {
        this.delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, BufferedImage icon) {
        this.delegate.setAuthor(name, url, icon);
        return this;
    }

    public EmbedBuilder setAuthor(String name, String url, BufferedImage icon, String fileType) {
        this.delegate.setAuthor(name, url, icon, fileType);
        return this;
    }

    public EmbedBuilder setThumbnail(String url) {
        this.delegate.setThumbnail(url);
        return this;
    }

    public EmbedBuilder setThumbnail(Icon thumbnail) {
        this.delegate.setThumbnail(thumbnail);
        return this;
    }

    public EmbedBuilder setThumbnail(File thumbnail) {
        this.delegate.setThumbnail(thumbnail);
        return this;
    }

    public EmbedBuilder setThumbnail(InputStream thumbnail) {
        this.delegate.setThumbnail(thumbnail);
        return this;
    }

    public EmbedBuilder setThumbnail(InputStream thumbnail, String fileType) {
        this.delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    public EmbedBuilder setThumbnail(byte[] thumbnail) {
        this.delegate.setThumbnail(thumbnail);
        return this;
    }

    public EmbedBuilder setThumbnail(byte[] thumbnail, String fileType) {
        this.delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    public EmbedBuilder setThumbnail(BufferedImage thumbnail) {
        this.delegate.setThumbnail(thumbnail);
        return this;
    }

    public EmbedBuilder setThumbnail(BufferedImage thumbnail, String fileType) {
        this.delegate.setThumbnail(thumbnail, fileType);
        return this;
    }

    public EmbedBuilder addInlineField(String name, String value) {
        this.delegate.addField(name, value, true);
        return this;
    }

    public EmbedBuilder addField(String name, String value) {
        this.delegate.addField(name, value, false);
        return this;
    }

    public EmbedBuilder addField(String name, String value, boolean inline) {
        this.delegate.addField(name, value, inline);
        return this;
    }

    public EmbedBuilder updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater) {
        this.delegate.updateFields(predicate, updater);
        return this;
    }

    public EmbedBuilder updateAllFields(Consumer<EditableEmbedField> updater) {
        this.delegate.updateFields(field -> true, updater);
        return this;
    }

    public EmbedBuilder removeFields(Predicate<EmbedField> predicate) {
        this.delegate.removeFields(predicate);
        return this;
    }

    public EmbedBuilder removeAllFields() {
        this.delegate.removeFields(field -> true);
        return this;
    }

    public boolean requiresAttachments() {
        return this.delegate.requiresAttachments();
    }
}

