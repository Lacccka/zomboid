/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.CertainMessageEvent;
import org.javacord.core.event.message.MessageEventImpl;

public abstract class CertainMessageEventImpl
extends MessageEventImpl
implements CertainMessageEvent {
    private final Message message;

    public CertainMessageEventImpl(Message message) {
        super(message.getApi(), message.getId(), message.getChannel());
        this.message = message;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }
}

