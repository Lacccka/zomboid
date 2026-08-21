/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

public interface Reaction {
    public static CompletableFuture<Set<User>> getUsers(DiscordApi api, long channelId, long messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().getUsersWhoReactedWithEmoji(channelId, messageId, emoji);
    }

    public static CompletableFuture<Set<User>> getUsers(DiscordApi api, String channelId, String messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().getUsersWhoReactedWithEmoji(channelId, messageId, emoji);
    }

    default public CompletableFuture<Set<User>> getUsers() {
        return Reaction.getUsers(this.getMessage().getApi(), this.getMessage().getChannel().getId(), this.getMessage().getId(), this.getEmoji());
    }

    public static CompletableFuture<Void> removeUser(DiscordApi api, long channelId, long messageId, Emoji emoji, long userId) {
        return api.getUncachedMessageUtil().removeUserReactionByEmoji(channelId, messageId, emoji, userId);
    }

    public static CompletableFuture<Void> removeUser(DiscordApi api, String channelId, String messageId, Emoji emoji, String userId) {
        return api.getUncachedMessageUtil().removeUserReactionByEmoji(channelId, messageId, emoji, userId);
    }

    default public CompletableFuture<Void> removeUser(User user) {
        return Reaction.removeUser(this.getMessage().getApi(), this.getMessage().getChannel().getId(), this.getMessage().getId(), this.getEmoji(), user.getId());
    }

    public Message getMessage();

    public Emoji getEmoji();

    public int getCount();

    public boolean containsYou();

    default public CompletableFuture<Void> removeYourself() {
        return this.removeUser(this.getMessage().getApi().getYourself());
    }

    default public CompletableFuture<Void> remove() {
        return this.getUsers().thenCompose(users -> CompletableFuture.allOf((CompletableFuture[])users.stream().map(this::removeUser).toArray(CompletableFuture[]::new)));
    }
}

