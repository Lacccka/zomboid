/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.internal;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.internal.MessageBuilderBaseDelegate;
import org.javacord.api.interaction.InteractionBase;

public interface InteractionMessageBuilderDelegate
extends MessageBuilderBaseDelegate {
    public void setFlags(EnumSet<MessageFlag> var1);

    public CompletableFuture<Void> sendInitialResponse(InteractionBase var1);

    public CompletableFuture<Void> deleteInitialResponse(InteractionBase var1);

    public CompletableFuture<Message> editOriginalResponse(InteractionBase var1);

    public CompletableFuture<Message> sendFollowupMessage(InteractionBase var1);

    public CompletableFuture<Void> updateOriginalMessage(InteractionBase var1);

    public CompletableFuture<Void> deleteFollowupMessage(InteractionBase var1, String var2);

    public CompletableFuture<Message> editFollowupMessage(InteractionBase var1, String var2);

    public void copy(InteractionBase var1);
}

