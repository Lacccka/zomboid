/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message.reaction;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.core.event.message.reaction.SingleReactionEventImpl;

public class ReactionRemoveEventImpl
extends SingleReactionEventImpl
implements ReactionRemoveEvent {
    public ReactionRemoveEventImpl(DiscordApi api, long messageId, TextChannel channel, Emoji emoji, long userId) {
        super(api, messageId, channel, emoji, userId);
    }
}

