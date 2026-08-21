/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.core.event.message.OptionalMessageEventImpl;

public class MessageDeleteEventImpl
extends OptionalMessageEventImpl
implements MessageDeleteEvent {
    public MessageDeleteEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api, messageId, channel);
    }
}

