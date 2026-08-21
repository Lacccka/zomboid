/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.TextChannelEvent;

public interface MessageEvent
extends TextChannelEvent {
    public long getMessageId();

    public Optional<Server> getServer();

    public CompletableFuture<Void> deleteMessage();

    public CompletableFuture<Void> deleteMessage(String var1);

    public CompletableFuture<Message> editMessage(String var1);

    default public CompletableFuture<Message> editMessage(EmbedBuilder ... embeds) {
        return this.editMessage(Arrays.asList(embeds));
    }

    public CompletableFuture<Message> editMessage(List<EmbedBuilder> var1);

    default public CompletableFuture<Message> editMessage(String content, EmbedBuilder ... embeds) {
        return this.editMessage(content, Arrays.asList(embeds));
    }

    public CompletableFuture<Message> editMessage(String var1, List<EmbedBuilder> var2);

    public CompletableFuture<Void> addReactionToMessage(String var1);

    public CompletableFuture<Void> addReactionToMessage(Emoji var1);

    public CompletableFuture<Void> addReactionsToMessage(Emoji ... var1);

    public CompletableFuture<Void> addReactionsToMessage(String ... var1);

    public CompletableFuture<Void> removeAllReactionsFromMessage();

    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User var1, Emoji var2);

    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User var1, String var2);

    public CompletableFuture<Void> removeReactionByEmojiFromMessage(Emoji var1);

    public CompletableFuture<Void> removeReactionByEmojiFromMessage(String var1);

    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User var1, Emoji ... var2);

    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User var1, String ... var2);

    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(Emoji ... var1);

    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(String ... var1);

    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(Emoji var1);

    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(String var1);

    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(Emoji ... var1);

    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(String ... var1);

    public CompletableFuture<Void> pinMessage();

    public CompletableFuture<Void> unpinMessage();
}

