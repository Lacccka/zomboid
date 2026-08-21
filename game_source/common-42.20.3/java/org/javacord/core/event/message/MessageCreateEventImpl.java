/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.core.event.message.CertainMessageEventImpl;

public class MessageCreateEventImpl
extends CertainMessageEventImpl
implements MessageCreateEvent {
    public MessageCreateEventImpl(Message message) {
        super(message);
    }
}

