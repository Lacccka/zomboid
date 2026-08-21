/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
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
import org.javacord.api.interaction.callback.ExtendedInteractionMessageBuilderBase;
import org.javacord.api.util.internal.DelegateFactory;

public class InteractionMessageBuilder
implements ExtendedInteractionMessageBuilderBase<InteractionMessageBuilder> {
    protected final InteractionMessageBuilderDelegate delegate = DelegateFactory.createInteractionMessageBuilderDelegate();

    public CompletableFuture<InteractionMessageBuilder> sendInitialResponse(InteractionBase interaction) {
        CompletableFuture<InteractionMessageBuilder> future = new CompletableFuture<InteractionMessageBuilder>();
        ((CompletableFuture)this.delegate.sendInitialResponse(interaction).thenRun(() -> future.complete(this))).exceptionally(e -> {
            future.completeExceptionally((Throwable)e);
            return null;
        });
        return future;
    }

    public CompletableFuture<Message> editOriginalResponse(InteractionBase interaction) {
        return this.delegate.editOriginalResponse(interaction);
    }

    public CompletableFuture<Message> sendFollowupMessage(InteractionBase interaction) {
        return this.delegate.sendFollowupMessage(interaction);
    }

    public CompletableFuture<Message> editFollowupMessage(InteractionBase interaction, long messageId) {
        return this.editFollowupMessage(interaction, Long.toUnsignedString(messageId));
    }

    public CompletableFuture<Message> editFollowupMessage(InteractionBase interaction, String messageId) {
        return this.delegate.editFollowupMessage(interaction, messageId);
    }

    public CompletableFuture<Void> updateOriginalMessage(InteractionBase interaction) {
        return this.delegate.updateOriginalMessage(interaction);
    }

    public CompletableFuture<Void> deleteInitialResponse(InteractionBase interaction) {
        return this.delegate.deleteInitialResponse(interaction);
    }

    public CompletableFuture<Void> deleteFollowupMessage(InteractionBase interaction, String messageId) {
        return this.delegate.deleteFollowupMessage(interaction, messageId);
    }

    @Override
    public InteractionMessageBuilder appendCode(String language, String code) {
        this.delegate.appendCode(language, code);
        return this;
    }

    @Override
    public InteractionMessageBuilder append(String message, MessageDecoration ... decorations) {
        this.delegate.append(message, decorations);
        return this;
    }

    @Override
    public InteractionMessageBuilder append(Mentionable entity) {
        this.delegate.append(entity);
        return this;
    }

    @Override
    public InteractionMessageBuilder append(Object object) {
        this.delegate.append(object);
        return this;
    }

    @Override
    public InteractionMessageBuilder appendNamedLink(String name, String url) {
        this.delegate.appendNamedLink(name, url);
        return this;
    }

    @Override
    public InteractionMessageBuilder appendNewLine() {
        this.delegate.appendNewLine();
        return this;
    }

    @Override
    public InteractionMessageBuilder setContent(String content) {
        this.delegate.setContent(content);
        return this;
    }

    @Override
    public InteractionMessageBuilder addEmbed(EmbedBuilder embed) {
        this.delegate.addEmbed(embed);
        return this;
    }

    @Override
    public InteractionMessageBuilder addEmbeds(EmbedBuilder ... embeds) {
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this;
    }

    @Override
    public InteractionMessageBuilder addEmbeds(List<EmbedBuilder> embeds) {
        this.delegate.addEmbeds(embeds);
        return this;
    }

    @Override
    public InteractionMessageBuilder addComponents(HighLevelComponent ... components) {
        this.delegate.addComponents(components);
        return this;
    }

    @Override
    public InteractionMessageBuilder removeAllComponents() {
        this.delegate.removeAllComponents();
        return this;
    }

    @Override
    public InteractionMessageBuilder removeComponent(int index) {
        this.delegate.removeComponent(index);
        return this;
    }

    @Override
    public InteractionMessageBuilder removeComponent(HighLevelComponent component) {
        this.delegate.removeComponent(component);
        return this;
    }

    @Override
    public InteractionMessageBuilder removeEmbed(EmbedBuilder embed) {
        this.delegate.removeEmbed(embed);
        return this;
    }

    @Override
    public InteractionMessageBuilder removeEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeEmbeds(embeds);
        return this;
    }

    @Override
    public InteractionMessageBuilder removeAllEmbeds() {
        this.delegate.removeAllEmbeds();
        return this;
    }

    @Override
    public InteractionMessageBuilder setTts(boolean tts) {
        this.delegate.setTts(tts);
        return this;
    }

    @Override
    public InteractionMessageBuilder setAllowedMentions(AllowedMentions allowedMentions) {
        this.delegate.setAllowedMentions(allowedMentions);
        return this;
    }

    @Override
    public InteractionMessageBuilder setFlags(MessageFlag ... messageFlags) {
        this.setFlags((EnumSet)EnumSet.copyOf(Arrays.asList(messageFlags)));
        return this;
    }

    @Override
    public InteractionMessageBuilder setFlags(EnumSet<MessageFlag> messageFlags) {
        this.delegate.setFlags(messageFlags);
        return this;
    }

    @Override
    public StringBuilder getStringBuilder() {
        return this.delegate.getStringBuilder();
    }

    @Override
    public InteractionMessageBuilder copy(Message message) {
        this.delegate.copy(message);
        return this;
    }

    @Override
    public InteractionMessageBuilder copy(InteractionBase interaction) {
        this.delegate.copy(interaction);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(BufferedImage image, String fileName) {
        this.addAttachment(image, fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, fileName, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(File file) {
        this.addAttachment(file, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(File file, String description) {
        this.delegate.addAttachment(file, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(Icon icon) {
        this.delegate.addAttachment(icon, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(Icon icon, String description) {
        this.delegate.addAttachment(icon, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(URL url) {
        this.delegate.addAttachment(url, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(URL url, String description) {
        this.delegate.addAttachment(url, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(byte[] bytes, String fileName) {
        this.addAttachment(bytes, fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, fileName, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(InputStream stream, String fileName) {
        this.addAttachment(stream, fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachment(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, fileName, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        this.addAttachment(image, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, "SPOILER_" + fileName, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(File file) {
        this.addAttachmentAsSpoiler(file, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(File file, String description) {
        this.delegate.addAttachmentAsSpoiler(file, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(Icon icon) {
        this.addAttachmentAsSpoiler(icon, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(Icon icon, String description) {
        this.delegate.addAttachmentAsSpoiler(icon, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(URL url) {
        this.addAttachmentAsSpoiler(url, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(URL url, String description) {
        this.delegate.addAttachmentAsSpoiler(url, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        this.addAttachment(bytes, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, "SPOILER_" + fileName, description);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(InputStream stream, String fileName) {
        this.addAttachment(stream, "SPOILER_" + fileName, null);
        return this;
    }

    @Override
    public InteractionMessageBuilder addAttachmentAsSpoiler(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, "SPOILER_" + fileName, description);
        return this;
    }
}

