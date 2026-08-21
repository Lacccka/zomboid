/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommandPermissions;
import org.javacord.api.interaction.ServerApplicationCommandPermissions;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandPermissionsImpl;

public class ServerApplicationCommandPermissionsImpl
implements ServerApplicationCommandPermissions {
    private final long id;
    private final long applicationId;
    private final Server server;
    private final Set<ApplicationCommandPermissions> permissions;

    public ServerApplicationCommandPermissionsImpl(DiscordApiImpl api, JsonNode data) {
        this.id = data.get("id").asLong();
        this.applicationId = data.get("application_id").asLong();
        this.server = api.getPossiblyUnreadyServerById(data.get("guild_id").asLong()).orElseThrow(AssertionError::new);
        this.permissions = new HashSet<ApplicationCommandPermissions>();
        for (JsonNode jsonNode : data.get("permissions")) {
            this.permissions.add(new ApplicationCommandPermissionsImpl(this.server, jsonNode));
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public long getApplicationId() {
        return this.applicationId;
    }

    @Override
    public long getServerId() {
        return this.server.getId();
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public Set<ApplicationCommandPermissions> getPermissions() {
        return this.permissions;
    }
}

