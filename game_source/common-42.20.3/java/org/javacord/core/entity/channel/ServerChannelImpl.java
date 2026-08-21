/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.listener.channel.server.InternalServerChannelAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerChannelImpl
implements ServerChannel,
InternalServerChannelAttachableListenerManager {
    private final DiscordApiImpl api;
    private final long id;
    private volatile String name;
    private final ServerImpl server;
    private final ChannelType type;

    public ServerChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        this.api = api;
        this.server = server;
        this.id = Long.parseLong(data.get("id").asText());
        this.name = data.get("name").asText();
        this.type = ChannelType.fromId(data.get("type").asInt(-1));
        api.addChannelToCache(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public CompletableFuture<Set<RichInvite>> getInvites() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.CHANNEL_INVITE).setUrlParameters(this.getIdAsString()).execute(result -> {
            HashSet<InviteImpl> invites = new HashSet<InviteImpl>();
            for (JsonNode inviteJson : result.getJsonBody()) {
                invites.add(new InviteImpl(this.getApi(), inviteJson));
            }
            return Collections.unmodifiableSet(invites);
        });
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.CHANNEL).setUrlParameters(this.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public ChannelType getType() {
        return this.type;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("ServerChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

