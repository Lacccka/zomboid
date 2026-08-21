/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageReplyEvent;
import org.javacord.core.event.message.CertainMessageEventImpl;

public class MessageReplyEventImpl
extends CertainMessageEventImpl
implements MessageReplyEvent {
    private final Message referencedMessage;

    public MessageReplyEventImpl(Message message, Message referencedMessage) {
        super(message);
        this.referencedMessage = referencedMessage;
    }

    @Override
    public Message getReferencedMessage() {
        return this.referencedMessage;
    }
}

