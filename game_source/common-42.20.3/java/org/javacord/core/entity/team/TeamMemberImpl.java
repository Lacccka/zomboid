/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.team;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.team.TeamMember;
import org.javacord.api.entity.team.TeamMembershipState;
import org.javacord.core.DiscordApiImpl;

public class TeamMemberImpl
implements TeamMember {
    private final DiscordApiImpl api;
    private final long id;
    private final TeamMembershipState membershipState;

    TeamMemberImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("user").get("id").asLong();
        this.membershipState = TeamMembershipState.fromId(data.get("membership_state").asInt());
    }

    @Override
    public TeamMembershipState getMembershipState() {
        return this.membershipState;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }
}

