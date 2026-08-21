/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelUpdater;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.internal.ServerTextChannelUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerTextChannelUpdater
extends RegularServerChannelUpdater<ServerTextChannelUpdater> {
    private final ServerTextChannelUpdaterDelegate delegate;

    public ServerTextChannelUpdater(ServerTextChannel channel) {
        super(DelegateFactory.createServerTextChannelUpdaterDelegate(channel));
        this.delegate = (ServerTextChannelUpdaterDelegate)this.regularServerChannelUpdaterDelegate;
    }

    public ServerTextChannelUpdater setTopic(String topic) {
        this.delegate.setTopic(topic);
        return this;
    }

    public ServerTextChannelUpdater setNsfwFlag(boolean nsfw) {
        this.delegate.setNsfwFlag(nsfw);
        return this;
    }

    public ServerTextChannelUpdater setCategory(ChannelCategory category) {
        this.delegate.setCategory(category);
        return this;
    }

    public ServerTextChannelUpdater removeCategory() {
        this.delegate.removeCategory();
        return this;
    }

    public ServerTextChannelUpdater setSlowmodeDelayInSeconds(int delay) {
        this.delegate.setSlowmodeDelayInSeconds(delay);
        return this;
    }

    public ServerTextChannelUpdater unsetSlowmode() {
        return this.setSlowmodeDelayInSeconds(0);
    }

    @Override
    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

