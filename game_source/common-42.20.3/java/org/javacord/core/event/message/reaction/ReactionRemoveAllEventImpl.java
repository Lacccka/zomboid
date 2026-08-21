/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message.reaction;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.core.event.message.RequestableMessageEventImpl;

public class ReactionRemoveAllEventImpl
extends RequestableMessageEventImpl
implements ReactionRemoveAllEvent {
    public ReactionRemoveAllEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api, messageId, channel);
    }
}

