/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelUpdater;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerVoiceChannelUpdater
extends RegularServerChannelUpdater<ServerVoiceChannelUpdater> {
    private final ServerVoiceChannelUpdaterDelegate delegate;

    public ServerVoiceChannelUpdater(ServerVoiceChannel channel) {
        super(DelegateFactory.createServerVoiceChannelUpdaterDelegate(channel));
        this.delegate = (ServerVoiceChannelUpdaterDelegate)this.regularServerChannelUpdaterDelegate;
    }

    public ServerVoiceChannelUpdater setBitrate(int bitrate) {
        this.delegate.setBitrate(bitrate);
        return this;
    }

    public ServerVoiceChannelUpdater setUserLimit(int userLimit) {
        this.delegate.setUserLimit(userLimit);
        return this;
    }

    public ServerVoiceChannelUpdater removeUserLimit() {
        this.delegate.removeUserLimit();
        return this;
    }

    public ServerVoiceChannelUpdater setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public ServerVoiceChannelUpdater removeCategory() {
        this.delegate.removeCategory();
        return this;
    }

    public ServerVoiceChannelUpdater setNsfw(Boolean nsfw) {
        this.delegate.setNsfw(nsfw);
        return this;
    }

    @Override
    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

