/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.team;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.team.TeamMember;
import org.javacord.api.entity.user.User;

public interface Team
extends DiscordEntity,
Nameable {
    public Optional<Icon> getIcon();

    public Set<TeamMember> getTeamMembers();

    public long getOwnerId();

    @Override
    public String getName();

    default public CompletableFuture<User> requestOwner() {
        return this.getApi().getUserById(this.getOwnerId());
    }
}

