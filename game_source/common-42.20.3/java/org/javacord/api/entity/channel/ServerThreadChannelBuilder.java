/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannelBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.internal.ServerThreadChannelBuilderDelegate;
import org.javacord.api.entity.message.Message;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerThreadChannelBuilder
extends ServerChannelBuilder<ServerThreadChannelBuilder> {
    private final ServerThreadChannelBuilderDelegate delegate;

    public ServerThreadChannelBuilder(ServerTextChannel serverTextChannel, ChannelType threadType, String name) {
        super(ServerThreadChannelBuilder.class, DelegateFactory.createServerThreadChannelBuilderDelegate(serverTextChannel));
        this.delegate = (ServerThreadChannelBuilderDelegate)((ServerChannelBuilder)this).delegate;
        this.delegate.setName(name);
        this.delegate.setChannelType(threadType);
    }

    public ServerThreadChannelBuilder(Message message, String name) {
        super(ServerThreadChannelBuilder.class, DelegateFactory.createServerThreadChannelBuilderDelegate(message));
        this.delegate = (ServerThreadChannelBuilderDelegate)((ServerChannelBuilder)this).delegate;
        this.delegate.setName(name);
    }

    public ServerThreadChannelBuilder setInvitableFlag(Boolean inviteable) {
        this.delegate.setInvitableFlag(inviteable);
        return this;
    }

    public ServerThreadChannelBuilder setSlowmodeDelayInSeconds(int delay) {
        this.delegate.setSlowmodeDelayInSeconds(delay);
        return this;
    }

    public ServerThreadChannelBuilder setAutoArchiveDuration(Integer autoArchiveDuration) {
        this.delegate.setAutoArchiveDuration(autoArchiveDuration);
        return this;
    }

    public ServerThreadChannelBuilder setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration) {
        this.delegate.setAutoArchiveDuration(autoArchiveDuration.asInt());
        return this;
    }

    public CompletableFuture<ServerThreadChannel> create() {
        return this.delegate.create();
    }
}

