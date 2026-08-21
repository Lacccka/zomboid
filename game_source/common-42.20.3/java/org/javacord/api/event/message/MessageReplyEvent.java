/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.CertainMessageEvent;

public interface MessageReplyEvent
extends CertainMessageEvent {
    public Message getReferencedMessage();
}

