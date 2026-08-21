/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMemberCountEvent;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeMemberCountEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeMemberCountEvent {
    private final int newMemberCount;
    private final int oldMemberCount;

    public ServerThreadChannelChangeMemberCountEventImpl(ServerThreadChannel channel, int newMemberCount, int oldMemberCount) {
        super(channel);
        this.newMemberCount = newMemberCount;
        this.oldMemberCount = oldMemberCount;
    }

    @Override
    public int getNewMemberCount() {
        return this.newMemberCount;
    }

    @Override
    public int getOldMemberCount() {
        return this.oldMemberCount;
    }
}

