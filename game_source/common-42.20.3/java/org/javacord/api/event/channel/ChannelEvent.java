/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.event.Event;

public interface ChannelEvent
extends Event {
    public Channel getChannel();
}

