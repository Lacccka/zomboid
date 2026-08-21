/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.net.URL;
import java.util.List;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageEvent;

public interface CertainMessageEvent
extends MessageEvent {
    public Message getMessage();

    default public boolean canYouReadContent() {
        return this.getMessage().canYouReadContent();
    }

    default public boolean isPrivateMessage() {
        return this.getMessage().isPrivateMessage();
    }

    default public boolean isServerMessage() {
        return this.getMessage().isServerMessage();
    }

    default public MessageAuthor getMessageAuthor() {
        return this.getMessage().getAuthor();
    }

    default public List<MessageAttachment> getMessageAttachments() {
        return this.getMessage().getAttachments();
    }

    default public String getMessageContent() {
        return this.getMessage().getContent();
    }

    default public String getReadableMessageContent() {
        return this.getMessage().getReadableContent();
    }

    default public URL getMessageLink() {
        return this.getMessage().getLink();
    }
}

