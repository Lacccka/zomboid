/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.user;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public interface Member
extends DiscordEntity,
Messageable,
Mentionable,
Permissionable {
    @Override
    default public String getMentionTag() {
        return "<@!" + this.getIdAsString() + ">";
    }

    default public String getDisplayName() {
        return this.getNickname().orElse(this.getUser().getName());
    }

    public Server getServer();

    public User getUser();

    public Optional<String> getNickname();

    public List<Role> getRoles();

    public boolean hasRole(Role var1);

    public Optional<Color> getRoleColor();

    public Optional<String> getServerAvatarHash();

    public Optional<Icon> getServerAvatar();

    public Optional<Icon> getServerAvatar(int var1);

    public Instant getJoinedAtTimestamp();

    public Optional<Instant> getServerBoostingSinceTimestamp();

    public boolean isMuted();

    public boolean isDeafened();

    public boolean isSelfMuted();

    public boolean isSelfDeafened();

    public boolean isPending();

    public Optional<Instant> getTimeout();
}

