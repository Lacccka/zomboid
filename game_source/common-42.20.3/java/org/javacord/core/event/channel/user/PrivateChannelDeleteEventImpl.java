/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.user;

import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.event.channel.user.PrivateChannelDeleteEvent;
import org.javacord.core.event.channel.user.PrivateChannelEventImpl;

public class PrivateChannelDeleteEventImpl
extends PrivateChannelEventImpl
implements PrivateChannelDeleteEvent {
    public PrivateChannelDeleteEventImpl(PrivateChannel channel) {
        super(channel);
    }
}

