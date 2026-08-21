/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.internal.ServerThreadChannelUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerThreadChannelUpdater
extends ServerChannelUpdater<ServerThreadChannelUpdater> {
    private final ServerThreadChannelUpdaterDelegate delegate;

    public ServerThreadChannelUpdater(ServerThreadChannel thread2) {
        super(thread2);
        this.delegate = DelegateFactory.createServerThreadChannelUpdaterDelegate(thread2);
    }

    public ServerThreadChannelUpdater setArchivedFlag(boolean archived) {
        this.delegate.setArchivedFlag(archived);
        return this;
    }

    public ServerThreadChannelUpdater setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration) {
        this.delegate.setAutoArchiveDuration(autoArchiveDuration);
        return this;
    }

    public ServerThreadChannelUpdater setLockedFlag(boolean locked) {
        this.delegate.setLockedFlag(locked);
        return this;
    }

    public ServerThreadChannelUpdater setInvitableFlag(boolean invitable) {
        this.delegate.setInvitableFlag(invitable);
        return this;
    }

    public ServerThreadChannelUpdater setSlowmodeDelayInSeconds(int delay) {
        this.delegate.setSlowmodeDelayInSeconds(delay);
        return this;
    }

    @Override
    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

