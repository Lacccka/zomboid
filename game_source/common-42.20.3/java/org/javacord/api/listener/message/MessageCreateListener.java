/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;
import org.javacord.api.listener.webhook.WebhookAttachableListener;

@FunctionalInterface
public interface MessageCreateListener
extends ServerAttachableListener,
UserAttachableListener,
WebhookAttachableListener,
TextChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onMessageCreate(MessageCreateEvent var1);
}

