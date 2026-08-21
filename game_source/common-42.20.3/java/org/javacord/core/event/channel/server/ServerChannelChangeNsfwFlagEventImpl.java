/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelChangeNsfwFlagEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelChangeNsfwFlagEventImpl
extends ServerChannelEventImpl
implements ServerChannelChangeNsfwFlagEvent {
    private final boolean newNsfwFlag;
    private final boolean oldNsfwFlag;

    public ServerChannelChangeNsfwFlagEventImpl(ServerChannel channel, boolean newNsfwFlag, boolean oldNsfwFlag) {
        super(channel);
        this.newNsfwFlag = newNsfwFlag;
        this.oldNsfwFlag = oldNsfwFlag;
    }

    @Override
    public boolean getNewNsfwFlag() {
        return this.newNsfwFlag;
    }

    @Override
    public boolean getOldNsfwFlag() {
        return this.oldNsfwFlag;
    }
}

