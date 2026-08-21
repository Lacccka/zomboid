/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.InteractionMessageBuilderDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.core.interaction.ExtendedInteractionMessageBuilderBaseImpl;
import org.javacord.core.interaction.InteractionImpl;

public class InteractionOriginalResponseUpdaterImpl
extends ExtendedInteractionMessageBuilderBaseImpl<InteractionOriginalResponseUpdater>
implements InteractionOriginalResponseUpdater {
    private final InteractionImpl interaction;

    public InteractionOriginalResponseUpdaterImpl(InteractionBase interaction, InteractionMessageBuilderDelegate delegate) {
        super(InteractionOriginalResponseUpdater.class, delegate);
        this.interaction = (InteractionImpl)interaction;
    }

    public InteractionOriginalResponseUpdaterImpl(InteractionBase interaction) {
        super(InteractionOriginalResponseUpdater.class);
        this.interaction = (InteractionImpl)interaction;
    }

    @Override
    public CompletableFuture<Message> update() {
        return this.delegate.editOriginalResponse(this.interaction);
    }

    @Override
    public CompletableFuture<Void> delete() {
        return this.delegate.deleteInitialResponse(this.interaction);
    }

    @Override
    public InteractionOriginalResponseUpdater appendCode(String language, String code) {
        this.delegate.appendCode(language, code);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater append(String message, MessageDecoration ... decorations) {
        this.delegate.append(message, decorations);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater append(Mentionable entity) {
        this.delegate.append(entity);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater append(Object object) {
        this.delegate.append(object);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater appendNewLine() {
        this.delegate.appendNewLine();
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater setContent(String content) {
        this.delegate.setContent(content);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addEmbed(EmbedBuilder embed) {
        this.delegate.addEmbed(embed);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addEmbeds(EmbedBuilder ... embeds) {
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addComponents(HighLevelComponent ... components) {
        this.delegate.addComponents(components);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater copy(Message message) {
        this.delegate.copy(message);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater copy(InteractionBase interaction) {
        this.delegate.copy(interaction);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeAllComponents() {
        this.delegate.removeAllComponents();
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeComponent(int index) {
        this.delegate.removeComponent(index);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeComponent(HighLevelComponent component) {
        this.delegate.removeComponent(component);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeEmbed(EmbedBuilder embed) {
        this.delegate.removeEmbed(embed);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeEmbeds(embeds);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater removeAllEmbeds() {
        this.delegate.removeAllEmbeds();
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater setTts(boolean tts) {
        this.delegate.setTts(tts);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(BufferedImage image, String fileName) {
        this.addAttachment(image, fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(File file) {
        this.addAttachment(file, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(File file, String description) {
        this.delegate.addAttachment(file, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(Icon icon) {
        this.addAttachment(icon, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(Icon icon, String description) {
        this.delegate.addAttachment(icon, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(URL url) {
        this.addAttachment(url, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(URL url, String description) {
        this.delegate.addAttachment(url, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(byte[] bytes, String fileName) {
        this.addAttachment(bytes, fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(InputStream stream, String fileName) {
        this.addAttachment(stream, fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachment(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        this.addAttachment(image, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, "SPOILER_" + fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(File file) {
        this.addAttachmentAsSpoiler(file, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(File file, String description) {
        this.delegate.addAttachmentAsSpoiler(file, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(Icon icon) {
        this.addAttachmentAsSpoiler(icon, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(Icon icon, String description) {
        this.delegate.addAttachmentAsSpoiler(icon, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(URL url) {
        this.addAttachmentAsSpoiler(url, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(URL url, String description) {
        this.delegate.addAttachmentAsSpoiler(url, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        this.addAttachment(bytes, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, "SPOILER_" + fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(InputStream stream, String fileName) {
        this.addAttachment(stream, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater addAttachmentAsSpoiler(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, "SPOILER_" + fileName, description);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater setAllowedMentions(AllowedMentions allowedMentions) {
        this.delegate.setAllowedMentions(allowedMentions);
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater setFlags(MessageFlag ... messageFlags) {
        this.setFlags((EnumSet)EnumSet.copyOf(Arrays.asList(messageFlags)));
        return this;
    }

    @Override
    public InteractionOriginalResponseUpdater setFlags(EnumSet<MessageFlag> messageFlags) {
        this.delegate.setFlags(messageFlags);
        return this;
    }
}

