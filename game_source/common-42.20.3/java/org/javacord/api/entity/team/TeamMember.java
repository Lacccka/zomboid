/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.team;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.team.TeamMembershipState;
import org.javacord.api.entity.user.User;

public interface TeamMember
extends DiscordEntity {
    public TeamMembershipState getMembershipState();

    @Override
    public long getId();

    default public CompletableFuture<User> requestUser() {
        return this.getApi().getUserById(this.getId());
    }
}

