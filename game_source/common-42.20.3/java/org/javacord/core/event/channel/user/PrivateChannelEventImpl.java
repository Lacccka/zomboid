/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.user;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.event.channel.user.PrivateChannelEvent;

public abstract class PrivateChannelEventImpl
implements PrivateChannelEvent {
    private final PrivateChannel channel;

    public PrivateChannelEventImpl(PrivateChannel channel) {
        this.channel = channel;
    }

    @Override
    public PrivateChannel getChannel() {
        return this.channel;
    }

    @Override
    public DiscordApi getApi() {
        return this.getChannel().getApi();
    }
}

