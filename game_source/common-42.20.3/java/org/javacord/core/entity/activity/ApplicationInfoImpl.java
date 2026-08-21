/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.ApplicationInfo;
import org.javacord.api.entity.ApplicationOwner;
import org.javacord.api.entity.team.Team;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.ApplicationOwnerImpl;
import org.javacord.core.entity.team.TeamImpl;

public class ApplicationInfoImpl
implements ApplicationInfo {
    private final long clientId;
    private final String name;
    private final String description;
    private final boolean publicBot;
    private final boolean botRequiresCodeGrant;
    private final ApplicationOwner owner;
    private final Team team;

    public ApplicationInfoImpl(DiscordApi api, JsonNode data) {
        this.clientId = data.get("id").asLong();
        this.name = data.get("name").asText();
        this.description = data.get("description").asText();
        this.publicBot = data.get("bot_public").asBoolean();
        this.botRequiresCodeGrant = data.get("bot_require_code_grant").asBoolean();
        this.team = data.hasNonNull("team") ? new TeamImpl((DiscordApiImpl)api, data.get("team")) : null;
        this.owner = this.team == null ? new ApplicationOwnerImpl(api, data.get("owner")) : null;
    }

    @Override
    public long getClientId() {
        return this.clientId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean isPublicBot() {
        return this.publicBot;
    }

    @Override
    public boolean botRequiresCodeGrant() {
        return this.botRequiresCodeGrant;
    }

    @Override
    public Optional<ApplicationOwner> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    @Override
    public Optional<Team> getTeam() {
        return Optional.ofNullable(this.team);
    }
}

