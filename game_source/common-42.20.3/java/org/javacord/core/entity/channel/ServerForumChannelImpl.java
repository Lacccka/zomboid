/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.forum.InternalServerForumChannelAttachableListenerManager;

public class ServerForumChannelImpl
extends RegularServerChannelImpl
implements ServerForumChannel,
InternalServerForumChannelAttachableListenerManager {
    private volatile long parentId;

    public ServerForumChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.parentId = Long.parseLong(data.has("parent_id") ? data.get("parent_id").asText("-1") : "-1");
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    @Override
    public Optional<ChannelCategory> getCategory() {
        return this.getServer().getChannelCategoryById(this.parentId);
    }

    @Override
    public CompletableFuture<Void> updateCategory(ChannelCategory category) {
        return null;
    }

    @Override
    public CompletableFuture<Void> removeCategory() {
        return null;
    }

    @Override
    public String getMentionTag() {
        return "<#" + this.getIdAsString() + ">";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return String.format("ServerForumChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

