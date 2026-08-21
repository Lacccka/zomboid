/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListenerManager;

public interface ServerChannel
extends Channel,
Nameable,
Deletable,
ServerChannelAttachableListenerManager {
    public Server getServer();

    default public InviteBuilder createInviteBuilder() {
        return new InviteBuilder(this);
    }

    public CompletableFuture<Set<RichInvite>> getInvites();

    default public ServerChannelUpdater createUpdater() {
        return new ServerChannelUpdater(this);
    }

    default public CompletableFuture<Void> updateName(String name) {
        return ((ServerChannelUpdater)this.createUpdater().setName(name)).update();
    }

    default public Optional<? extends ServerChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getChannelById(this.getId()));
    }

    @Override
    default public CompletableFuture<? extends ServerChannel> getLatestInstance() {
        Optional<? extends ServerChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

