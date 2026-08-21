/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.internal.ServerTextChannelBuilderDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerTextChannelBuilder
extends RegularServerChannelBuilder<ServerTextChannelBuilder> {
    private final ServerTextChannelBuilderDelegate delegate;

    public ServerTextChannelBuilder(Server server) {
        super(ServerTextChannelBuilder.class, DelegateFactory.createServerTextChannelBuilderDelegate(server));
        this.delegate = (ServerTextChannelBuilderDelegate)((RegularServerChannelBuilder)this).delegate;
    }

    public ServerTextChannelBuilder setTopic(String topic) {
        this.delegate.setTopic(topic);
        return this;
    }

    public ServerTextChannelBuilder setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public ServerTextChannelBuilder setSlowmodeDelayInSeconds(int delay) {
        this.delegate.setSlowmodeDelayInSeconds(delay);
        return this;
    }

    public CompletableFuture<ServerTextChannel> create() {
        return this.delegate.create();
    }
}

