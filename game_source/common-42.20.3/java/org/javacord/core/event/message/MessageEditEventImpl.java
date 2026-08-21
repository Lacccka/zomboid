/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.core.event.message.CertainMessageEventImpl;

public class MessageEditEventImpl
extends CertainMessageEventImpl
implements MessageEditEvent {
    private final Message oldMessage;
    private final boolean isActualEdit;

    public MessageEditEventImpl(DiscordApi api, long messageId, TextChannel channel, Message updatedMessage, Message oldMessage, boolean isActualEdit) {
        super(updatedMessage);
        this.oldMessage = oldMessage;
        this.isActualEdit = isActualEdit;
    }

    @Override
    public Optional<Message> getOldMessage() {
        return Optional.ofNullable(this.oldMessage);
    }

    @Override
    public boolean isActualEdit() {
        return this.isActualEdit;
    }
}

