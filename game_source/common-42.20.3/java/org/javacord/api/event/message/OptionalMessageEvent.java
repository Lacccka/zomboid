/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageEvent;

public interface OptionalMessageEvent
extends MessageEvent {
    public Optional<Message> getMessage();

    default public Optional<Boolean> canYouReadMessageContent() {
        return this.getMessage().map(Message::canYouReadContent);
    }

    default public Optional<MessageAuthor> getMessageAuthor() {
        return this.getMessage().map(Message::getAuthor);
    }

    default public Optional<List<MessageAttachment>> getMessageAttachments() {
        return this.getMessage().map(Message::getAttachments);
    }

    default public Optional<String> getMessageContent() {
        return this.getMessage().map(Message::getContent);
    }

    default public Optional<String> getReadableMessageContent() {
        return this.getMessage().map(Message::getReadableContent);
    }

    default public Optional<URL> getMessageLink() {
        return this.getMessage().map(Message::getLink);
    }
}

