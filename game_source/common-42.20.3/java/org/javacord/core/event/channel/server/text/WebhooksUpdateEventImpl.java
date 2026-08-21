/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.WebhooksUpdateEvent;
import org.javacord.core.event.channel.server.text.ServerTextChannelEventImpl;

public class WebhooksUpdateEventImpl
extends ServerTextChannelEventImpl
implements WebhooksUpdateEvent {
    public WebhooksUpdateEventImpl(ServerTextChannel channel) {
        super(channel);
    }
}

