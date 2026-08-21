/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.util.Optional;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.CertainMessageEvent;

public interface MessageEditEvent
extends CertainMessageEvent {
    @Override
    public Message getMessage();

    public Optional<Message> getOldMessage();

    public boolean isActualEdit();
}

