/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public interface Messageable {
    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, boolean tts, String nonce, InputStream stream, String fileName) {
        return this.sendMessage(content, embed, tts, nonce, stream, fileName, null);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, boolean tts, String nonce, InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)((MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbed(embed)).setTts(tts).setNonce(nonce)).addAttachment(stream, fileName, fileDescription)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, boolean tts, String nonce, File ... files) {
        MessageBuilder messageBuilder = (MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbed(embed)).setTts(tts).setNonce(nonce);
        for (File file : files) {
            messageBuilder.addAttachment(file);
        }
        return messageBuilder.send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, boolean tts, String nonce) {
        return ((MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbed(embed)).setTts(tts).setNonce(nonce)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).addEmbed(embed)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, HighLevelComponent ... components) {
        return this.sendMessage(content, Collections.singletonList(embed), components);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, File ... files) {
        return this.sendMessage(content, Collections.singletonList(embed), files);
    }

    default public CompletableFuture<Message> sendMessage(String content, List<EmbedBuilder> embeds) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbeds((List)embeds)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, List<EmbedBuilder> embeds, HighLevelComponent ... components) {
        return ((MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbeds((List)embeds)).addComponents(components)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, List<EmbedBuilder> embeds, File ... files) {
        MessageBuilder messageBuilder = (MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbeds((List)embeds);
        for (File file : files) {
            messageBuilder.addAttachment(file);
        }
        return messageBuilder.send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content) {
        return ((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder ... embeds) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).addEmbeds(embeds)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(HighLevelComponent ... components) {
        return ((MessageBuilder)new MessageBuilder().addComponents(components)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(HighLevelComponent component) {
        return ((MessageBuilder)new MessageBuilder().addComponents(new HighLevelComponent[]{component})).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, HighLevelComponent ... components) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).addComponents(components)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, File ... files) {
        MessageBuilder messageBuilder = (MessageBuilder)new MessageBuilder().append(content == null ? "" : content);
        for (File file : files) {
            messageBuilder.addAttachment(file);
        }
        return messageBuilder.send(this);
    }

    default public CompletableFuture<Message> sendMessage(File ... files) {
        MessageBuilder messageBuilder = new MessageBuilder();
        for (File file : files) {
            messageBuilder.addAttachment(file);
        }
        return messageBuilder.send(this);
    }

    default public CompletableFuture<Message> sendMessage(InputStream stream, String fileName) {
        return this.sendMessage(stream, fileName, null);
    }

    default public CompletableFuture<Message> sendMessage(InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)new MessageBuilder().addAttachment(stream, fileName, fileDescription)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, InputStream stream, String fileName) {
        return this.sendMessage(content, stream, fileName, null);
    }

    default public CompletableFuture<Message> sendMessage(String content, InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).addAttachment(stream, fileName, fileDescription)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, InputStream stream, String fileName) {
        return this.sendMessage(content, embed, stream, fileName, null);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbed(embed)).addAttachment(stream, fileName, fileDescription)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, List<EmbedBuilder> embeds, InputStream stream, String fileName) {
        return this.sendMessage(content, embeds, stream, fileName, null);
    }

    default public CompletableFuture<Message> sendMessage(String content, List<EmbedBuilder> embeds, InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)((MessageBuilder)((MessageBuilder)new MessageBuilder().append(content == null ? "" : content)).setEmbeds((List)embeds)).addAttachment(stream, fileName, fileDescription)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed) {
        return this.sendMessage(Collections.singletonList(embed));
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder ... embeds) {
        return this.sendMessage(Arrays.asList(embeds));
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, File ... files) {
        return this.sendMessage(Collections.singletonList(embed), files);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, HighLevelComponent ... components) {
        return this.sendMessage(Collections.singletonList(embed), components);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, InputStream stream, String fileName) {
        return this.sendMessage(Collections.singletonList(embed), stream, fileName);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, InputStream stream, String fileName, String fileDescription) {
        return this.sendMessage(Collections.singletonList(embed), stream, fileName, fileDescription);
    }

    default public CompletableFuture<Message> sendMessage(List<EmbedBuilder> embeds) {
        return ((MessageBuilder)new MessageBuilder().setEmbeds((List)embeds)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(List<EmbedBuilder> embeds, HighLevelComponent ... components) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().setEmbeds((List)embeds)).addComponents(components)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(List<EmbedBuilder> embeds, File ... files) {
        MessageBuilder messageBuilder = (MessageBuilder)new MessageBuilder().setEmbeds((List)embeds);
        for (File file : files) {
            messageBuilder.addAttachment(file);
        }
        return messageBuilder.send(this);
    }

    default public CompletableFuture<Message> sendMessage(List<EmbedBuilder> embeds, InputStream stream, String fileName) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().setEmbeds((List)embeds)).addAttachment(stream, fileName)).send(this);
    }

    default public CompletableFuture<Message> sendMessage(List<EmbedBuilder> embeds, InputStream stream, String fileName, String fileDescription) {
        return ((MessageBuilder)((MessageBuilder)new MessageBuilder().setEmbeds((List)embeds)).addAttachment(stream, fileName, fileDescription)).send(this);
    }
}

