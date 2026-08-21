/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.UncachedMessageAttachableListenerManager;

public interface UncachedMessageUtil
extends UncachedMessageAttachableListenerManager {
    default public CompletableFuture<Message> crossPost(long channelId, long messageId) {
        return this.crossPost(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId));
    }

    public CompletableFuture<Message> crossPost(String var1, String var2);

    public CompletableFuture<Void> delete(long var1, long var3);

    public CompletableFuture<Void> delete(String var1, String var2);

    public CompletableFuture<Void> delete(long var1, long var3, String var5);

    public CompletableFuture<Void> delete(String var1, String var2, String var3);

    public CompletableFuture<Void> delete(long var1, long ... var3);

    public CompletableFuture<Void> delete(String var1, String ... var2);

    public CompletableFuture<Void> delete(Message ... var1);

    public CompletableFuture<Void> delete(Iterable<Message> var1);

    public CompletableFuture<Message> edit(long var1, long var3, String var5);

    public CompletableFuture<Message> edit(String var1, String var2, String var3);

    default public CompletableFuture<Message> edit(long channelId, long messageId, EmbedBuilder ... embeds) {
        return this.edit(channelId, messageId, Arrays.asList(embeds));
    }

    public CompletableFuture<Message> edit(long var1, long var3, List<EmbedBuilder> var5);

    default public CompletableFuture<Message> edit(String channelId, String messageId, EmbedBuilder ... embeds) {
        return this.edit(channelId, messageId, Arrays.asList(embeds));
    }

    public CompletableFuture<Message> edit(String var1, String var2, List<EmbedBuilder> var3);

    default public CompletableFuture<Message> edit(long channelId, long messageId, String content, EmbedBuilder ... embeds) {
        return this.edit(channelId, messageId, content, Arrays.asList(embeds));
    }

    public CompletableFuture<Message> edit(long var1, long var3, String var5, List<EmbedBuilder> var6);

    default public CompletableFuture<Message> edit(String channelId, String messageId, String content, EmbedBuilder ... embeds) {
        return this.edit(channelId, messageId, content, Arrays.asList(embeds));
    }

    public CompletableFuture<Message> edit(String var1, String var2, String var3, List<EmbedBuilder> var4);

    default public CompletableFuture<Message> edit(long channelId, long messageId, String content, boolean updateContent, EmbedBuilder embed, boolean updateEmbed) {
        return this.edit(channelId, messageId, content, updateContent, Collections.singletonList(embed), updateEmbed);
    }

    public CompletableFuture<Message> edit(long var1, long var3, String var5, boolean var6, List<EmbedBuilder> var7, boolean var8);

    default public CompletableFuture<Message> edit(String channelId, String messageId, String content, boolean updateContent, EmbedBuilder embed, boolean updateEmbed) {
        return this.edit(channelId, messageId, content, updateContent, Collections.singletonList(embed), updateEmbed);
    }

    public CompletableFuture<Message> edit(String var1, String var2, String var3, boolean var4, List<EmbedBuilder> var5, boolean var6);

    public CompletableFuture<Message> removeContent(long var1, long var3);

    public CompletableFuture<Message> removeContent(String var1, String var2);

    public CompletableFuture<Message> removeEmbed(long var1, long var3);

    public CompletableFuture<Message> removeEmbed(String var1, String var2);

    public CompletableFuture<Message> removeContentAndEmbed(long var1, long var3);

    public CompletableFuture<Message> removeContentAndEmbed(String var1, String var2);

    public CompletableFuture<Void> addReaction(long var1, long var3, String var5);

    public CompletableFuture<Void> addReaction(String var1, String var2, String var3);

    public CompletableFuture<Void> addReaction(long var1, long var3, Emoji var5);

    public CompletableFuture<Void> addReaction(String var1, String var2, Emoji var3);

    public CompletableFuture<Void> removeAllReactions(long var1, long var3);

    public CompletableFuture<Void> removeAllReactions(String var1, String var2);

    public CompletableFuture<Void> pin(long var1, long var3);

    public CompletableFuture<Void> pin(String var1, String var2);

    public CompletableFuture<Void> unpin(long var1, long var3);

    public CompletableFuture<Void> unpin(String var1, String var2);

    public CompletableFuture<Set<User>> getUsersWhoReactedWithEmoji(long var1, long var3, Emoji var5);

    public CompletableFuture<Set<User>> getUsersWhoReactedWithEmoji(String var1, String var2, Emoji var3);

    public CompletableFuture<Void> removeUserReactionByEmoji(long var1, long var3, Emoji var5, long var6);

    public CompletableFuture<Void> removeUserReactionByEmoji(String var1, String var2, Emoji var3, String var4);
}

