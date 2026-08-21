/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.core.entity.AttachmentImpl;

public class MessageAttachmentImpl
extends AttachmentImpl
implements MessageAttachment {
    private final Message message;

    public MessageAttachmentImpl(Message message, JsonNode data) {
        super(message.getApi(), data);
        this.message = message;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }
}

