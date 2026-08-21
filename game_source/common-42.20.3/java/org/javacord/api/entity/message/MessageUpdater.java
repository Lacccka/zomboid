/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilderBase;

public class MessageUpdater
extends MessageBuilderBase<MessageUpdater> {
    private final Message message;

    public MessageUpdater(Message m) {
        super(MessageUpdater.class);
        this.message = m;
    }

    public CompletableFuture<Message> applyChanges() {
        return this.delegate.edit(this.message, false);
    }

    public CompletableFuture<Message> replaceMessage() {
        return this.delegate.edit(this.message, true);
    }

    public MessageUpdater removeExistingAttachment(Attachment attachment) {
        this.delegate.removeExistingAttachment(attachment);
        return this;
    }

    public MessageUpdater removeExistingAttachments() {
        this.delegate.removeExistingAttachments();
        return this;
    }

    public MessageUpdater removeExistingAttachments(Attachment ... attachments) {
        this.removeExistingAttachments(Arrays.asList(attachments));
        return this;
    }

    public MessageUpdater removeExistingAttachments(Collection<Attachment> attachments) {
        this.delegate.removeExistingAttachments(attachments);
        return this;
    }
}

