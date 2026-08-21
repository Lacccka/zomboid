/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.TextChannelUserEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.UserEventImpl;

public abstract class ChannelUserEventImpl
extends UserEventImpl
implements TextChannelUserEvent {
    private final TextChannel channel;

    public ChannelUserEventImpl(User user, Member member, TextChannel channel) {
        super(user);
        this.channel = channel;
    }

    @Override
    public TextChannel getChannel() {
        return this.channel;
    }
}

