/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.user;

import java.util.Optional;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.ChannelEvent;

public interface PrivateChannelEvent
extends ChannelEvent {
    @Override
    public PrivateChannel getChannel();

    default public Optional<User> getRecipient() {
        return this.getChannel().getRecipient();
    }
}

