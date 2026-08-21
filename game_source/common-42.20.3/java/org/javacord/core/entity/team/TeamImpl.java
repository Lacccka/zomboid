/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.team;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.team.Team;
import org.javacord.api.entity.team.TeamMember;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.team.TeamMemberImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class TeamImpl
implements Team {
    private static final Logger logger = LoggerUtil.getLogger(TeamImpl.class);
    private final DiscordApiImpl api;
    private final long id;
    private final long ownerId;
    private final String iconHash;
    private final String name;
    private final Set<TeamMember> members = new HashSet<TeamMember>();

    public TeamImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.iconHash = data.path("icon").asText(null);
        this.name = data.get("name").asText();
        this.ownerId = data.get("owner_user_id").asLong();
        for (JsonNode member : data.get("members")) {
            this.members.add(new TeamMemberImpl(api, member));
        }
    }

    @Override
    public Optional<Icon> getIcon() {
        if (this.iconHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/team-icons/" + this.getIdAsString() + "/" + this.iconHash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Set<TeamMember> getTeamMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    @Override
    public String getName() {
        return this.name;
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

