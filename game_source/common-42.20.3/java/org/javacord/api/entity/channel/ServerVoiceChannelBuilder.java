/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelBuilderDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerVoiceChannelBuilder
extends RegularServerChannelBuilder<ServerVoiceChannelBuilder> {
    private final ServerVoiceChannelBuilderDelegate delegate;

    public ServerVoiceChannelBuilder(Server server) {
        super(ServerVoiceChannelBuilder.class, DelegateFactory.createServerVoiceChannelBuilderDelegate(server));
        this.delegate = (ServerVoiceChannelBuilderDelegate)((RegularServerChannelBuilder)this).delegate;
    }

    public ServerVoiceChannelBuilder setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public ServerVoiceChannelBuilder setBitrate(int bitrate) {
        this.delegate.setBitrate(bitrate);
        return this;
    }

    public ServerVoiceChannelBuilder setUserlimit(int userlimit) {
        this.delegate.setUserlimit(userlimit);
        return this;
    }

    public CompletableFuture<ServerVoiceChannel> create() {
        return this.delegate.create();
    }
}

