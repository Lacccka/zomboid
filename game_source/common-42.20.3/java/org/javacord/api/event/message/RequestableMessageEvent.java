/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.OptionalMessageEvent;

public interface RequestableMessageEvent
extends OptionalMessageEvent {
    public CompletableFuture<Message> requestMessage();
}

