/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message.reaction;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.message.reaction.SingleReactionEventImpl;

public class ReactionAddEventImpl
extends SingleReactionEventImpl
implements ReactionAddEvent {
    public ReactionAddEventImpl(DiscordApi api, long messageId, TextChannel channel, Emoji emoji, long userId, Member member) {
        super(api, messageId, channel, emoji, userId);
    }

    @Override
    public CompletableFuture<Void> removeReaction() {
        return Reaction.removeUser(this.getApi(), this.getChannel().getId(), this.getMessageId(), this.getEmoji(), this.getUserId());
    }
}

