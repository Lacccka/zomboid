/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeSlowmodeEvent;
import org.javacord.core.event.channel.server.text.ServerTextChannelEventImpl;

public class ServerTextChannelChangeSlowmodeEventImpl
extends ServerTextChannelEventImpl
implements ServerTextChannelChangeSlowmodeEvent {
    private final int oldDelay;
    private final int newDelay;

    public ServerTextChannelChangeSlowmodeEventImpl(ServerTextChannel channel, int oldDelay, int newDelay) {
        super(channel);
        this.oldDelay = oldDelay;
        this.newDelay = newDelay;
    }

    @Override
    public int getOldDelayInSeconds() {
        return this.oldDelay;
    }

    @Override
    public int getNewDelayInSeconds() {
        return this.newDelay;
    }
}

