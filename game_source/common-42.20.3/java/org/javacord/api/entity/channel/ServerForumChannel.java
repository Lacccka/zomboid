/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerForumChannelUpdater;
import org.javacord.api.listener.channel.server.forum.ServerForumChannelAttachableListenerManager;

public interface ServerForumChannel
extends RegularServerChannel,
Mentionable,
Categorizable,
ServerForumChannelAttachableListenerManager {
    @Override
    default public ServerForumChannelUpdater createUpdater() {
        return new ServerForumChannelUpdater(this);
    }

    @Override
    default public CompletableFuture<Void> updateCategory(ChannelCategory category) {
        return this.createUpdater().setCategory(category).update();
    }

    @Override
    default public CompletableFuture<Void> removeCategory() {
        return this.createUpdater().removeCategory().update();
    }
}

