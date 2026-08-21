/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.user;

import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.core.event.channel.user.PrivateChannelEventImpl;

public class PrivateChannelCreateEventImpl
extends PrivateChannelEventImpl
implements PrivateChannelCreateEvent {
    public PrivateChannelCreateEventImpl(PrivateChannel channel) {
        super(channel);
    }
}

