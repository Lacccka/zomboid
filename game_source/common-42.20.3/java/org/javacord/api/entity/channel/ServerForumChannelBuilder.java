/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelBuilder;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.internal.ServerForumChannelBuilderDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerForumChannelBuilder
extends RegularServerChannelBuilder<ServerForumChannelBuilder> {
    private final ServerForumChannelBuilderDelegate delegate;

    public ServerForumChannelBuilder(Server server) {
        super(ServerForumChannelBuilder.class, DelegateFactory.createServerForumChannelBuilderDelegate(server));
        this.delegate = (ServerForumChannelBuilderDelegate)((RegularServerChannelBuilder)this).delegate;
    }

    public ServerForumChannelBuilder setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public CompletableFuture<ServerForumChannel> create() {
        return this.delegate.create();
    }
}

