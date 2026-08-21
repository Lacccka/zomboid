/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;

public interface MessageBuilderBaseDelegate {
    public void addComponents(HighLevelComponent ... var1);

    public void addActionRow(LowLevelComponent ... var1);

    public void appendCode(String var1, String var2);

    public void append(String var1, MessageDecoration ... var2);

    public void append(Mentionable var1);

    public void append(Object var1);

    public void appendNamedLink(String var1, String var2);

    public void appendNewLine();

    public void copy(Message var1);

    public void setContent(String var1);

    public void removeExistingAttachment(Attachment var1);

    public void removeExistingAttachments();

    public void removeExistingAttachments(Collection<Attachment> var1);

    public void addEmbed(EmbedBuilder var1);

    public void removeAllEmbeds();

    public void addEmbeds(List<EmbedBuilder> var1);

    public void removeEmbed(EmbedBuilder var1);

    public void removeEmbeds(EmbedBuilder ... var1);

    public void removeComponent(int var1);

    public void removeComponent(HighLevelComponent var1);

    public void removeAllComponents();

    public void setTts(boolean var1);

    public void addAttachment(BufferedImage var1, String var2, String var3);

    public void addAttachment(File var1, String var2);

    public void addAttachment(Icon var1, String var2);

    public void addAttachment(URL var1, String var2);

    public void addAttachment(byte[] var1, String var2, String var3);

    public void addAttachment(InputStream var1, String var2, String var3);

    public void addAttachmentAsSpoiler(File var1, String var2);

    public void addAttachmentAsSpoiler(Icon var1, String var2);

    public void addAttachmentAsSpoiler(URL var1, String var2);

    public void setAllowedMentions(AllowedMentions var1);

    public void replyTo(long var1, boolean var3);

    public void setNonce(String var1);

    public void addSticker(long var1);

    public void addStickers(Collection<Long> var1);

    public StringBuilder getStringBuilder();

    public CompletableFuture<Message> send(User var1);

    public CompletableFuture<Message> send(TextChannel var1);

    public CompletableFuture<Message> send(IncomingWebhook var1);

    public CompletableFuture<Message> send(Messageable var1);

    public CompletableFuture<Message> edit(Message var1, boolean var2);

    public CompletableFuture<Message> sendWithWebhook(DiscordApi var1, String var2, String var3);
}

