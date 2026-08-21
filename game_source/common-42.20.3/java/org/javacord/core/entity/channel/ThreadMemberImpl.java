/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.server.Server;

public class ThreadMemberImpl
implements ThreadMember {
    private final long id;
    private final long userId;
    private final Instant joinTimestamp;
    private final int flags;
    private final DiscordApi api;
    private final Server server;

    public ThreadMemberImpl(DiscordApi api, Server server, JsonNode data) {
        this(api, server, data, data.get("id").asLong(), data.get("user_id").asLong());
    }

    public ThreadMemberImpl(DiscordApi api, Server server, JsonNode data, long id, long userId) {
        this.api = api;
        this.server = server;
        this.id = id;
        this.userId = userId;
        this.joinTimestamp = OffsetDateTime.parse(data.get("join_timestamp").asText()).toInstant();
        this.flags = data.get("flags").asInt();
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
    public Server getServer() {
        return this.server;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public Instant getJoinTimestamp() {
        return this.joinTimestamp;
    }

    @Override
    public int getFlags() {
        return this.flags;
    }
}

