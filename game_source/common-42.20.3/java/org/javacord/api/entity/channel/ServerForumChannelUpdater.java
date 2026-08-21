/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelUpdater;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.internal.ServerForumChannelUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerForumChannelUpdater
extends RegularServerChannelUpdater<ServerForumChannelUpdater> {
    private final ServerForumChannelUpdaterDelegate delegate;

    public ServerForumChannelUpdater(ServerForumChannel channel) {
        super(DelegateFactory.createServerForumChannelUpdaterDelegate(channel));
        this.delegate = (ServerForumChannelUpdaterDelegate)this.regularServerChannelUpdaterDelegate;
    }

    public ServerForumChannelUpdater setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public ServerForumChannelUpdater removeCategory() {
        this.delegate.removeCategory();
        return this;
    }

    @Override
    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

