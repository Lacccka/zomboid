/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.user;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.UserImpl;

public final class MemberImpl
implements Member {
    private static final int DEFAULT_AVATAR_SIZE = 1024;
    private final DiscordApiImpl api;
    private final ServerImpl server;
    private final UserImpl user;
    private final boolean pending;
    private final String nickname;
    private final List<Long> roleIds;
    private final String avatarHash;
    private final String joinedAt;
    private final String serverBoostingSince;
    private final boolean deafened;
    private final boolean muted;
    private final boolean selfDeafened;
    private final boolean selfMuted;
    private final Instant communicationDisabledUntil;

    public MemberImpl(DiscordApiImpl api, ServerImpl server, JsonNode data, UserImpl user) {
        this.api = api;
        this.server = server;
        this.selfMuted = false;
        this.selfDeafened = false;
        this.user = data.hasNonNull("user") ? new UserImpl(api, data.get("user"), this, null) : user;
        this.nickname = data.hasNonNull("nick") ? data.get("nick").asText() : null;
        this.roleIds = new ArrayList<Long>();
        for (JsonNode roleIdJson : data.get("roles")) {
            this.roleIds.add(roleIdJson.asLong());
        }
        this.roleIds.add(server.getEveryoneRole().getId());
        this.avatarHash = data.hasNonNull("avatar") ? data.get("avatar").asText() : null;
        this.joinedAt = data.get("joined_at").asText();
        this.serverBoostingSince = data.hasNonNull("premium_since") ? data.get("premium_since").asText() : null;
        this.pending = data.hasNonNull("pending") ? data.get("pending").asBoolean() : false;
        this.deafened = data.hasNonNull("deaf") ? data.get("deaf").asBoolean() : false;
        this.muted = data.hasNonNull("mute") ? data.get("mute").asBoolean() : false;
        this.communicationDisabledUntil = data.hasNonNull("communication_disabled_until") ? OffsetDateTime.parse(data.get("communication_disabled_until").asText()).toInstant() : null;
    }

    private MemberImpl(DiscordApiImpl api, ServerImpl server, UserImpl user, String nickname, List<Long> roleIds, String avatarHash, String joinedAt, String serverBoostingSince, boolean deafened, boolean muted, boolean selfDeafened, boolean selfMuted, boolean pending, Instant communicationDisabledUntil) {
        this.api = api;
        this.server = server;
        this.user = user;
        this.nickname = nickname;
        this.roleIds = roleIds;
        this.avatarHash = avatarHash;
        this.joinedAt = joinedAt;
        this.serverBoostingSince = serverBoostingSince;
        this.muted = muted;
        this.deafened = deafened;
        this.selfMuted = selfMuted;
        this.selfDeafened = selfDeafened;
        this.pending = pending;
        this.communicationDisabledUntil = communicationDisabledUntil;
    }

    public MemberImpl setUser(UserImpl user) {
        return new MemberImpl(this.api, this.server, user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setPartialUser(JsonNode partialUserJson) {
        return new MemberImpl(this.api, this.server, this.user.replacePartialUserData(partialUserJson), this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setRoleIds(List<Long> roleIds) {
        roleIds.add(this.server.getEveryoneRole().getId());
        return new MemberImpl(this.api, this.server, this.user, this.nickname, roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public List<Long> getRoleIds() {
        return this.roleIds;
    }

    public MemberImpl setNickname(String nickname) {
        return new MemberImpl(this.api, this.server, this.user, nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setTimeout(Instant timeout2) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, timeout2);
    }

    public MemberImpl setServerBoostingSince(String serverBoostingSince) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, serverBoostingSince, this.deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public String getServerBoostingSince() {
        return this.serverBoostingSince;
    }

    public MemberImpl setMuted(boolean muted) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setDeafened(boolean deafened) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, deafened, this.muted, this.selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setSelfMuted(boolean selfMuted) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, this.selfDeafened, selfMuted, this.pending, this.communicationDisabledUntil);
    }

    public MemberImpl setSelfDeafened(boolean selfDeafened) {
        return new MemberImpl(this.api, this.server, this.user, this.nickname, this.roleIds, this.avatarHash, this.joinedAt, this.serverBoostingSince, this.deafened, this.muted, selfDeafened, this.selfMuted, this.pending, this.communicationDisabledUntil);
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.user.getId();
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Optional<String> getNickname() {
        return Optional.ofNullable(this.nickname);
    }

    @Override
    public List<Role> getRoles() {
        return this.roleIds.stream().map(this.server::getRoleById).filter(Optional::isPresent).map(Optional::get).sorted().collect(Collectors.toList());
    }

    @Override
    public boolean hasRole(Role role) {
        return this.roleIds.contains(role.getId());
    }

    @Override
    public Optional<Color> getRoleColor() {
        return this.getRoles().stream().filter(role -> role.getColor().isPresent()).max(Comparator.comparingInt(Role::getRawPosition)).flatMap(Role::getColor);
    }

    @Override
    public Optional<String> getServerAvatarHash() {
        return Optional.ofNullable(this.avatarHash);
    }

    @Override
    public Optional<Icon> getServerAvatar() {
        return this.getServerAvatar(1024);
    }

    @Override
    public Optional<Icon> getServerAvatar(int size) {
        if (this.avatarHash != null) {
            StringBuilder url = new StringBuilder("https://cdn.discordapp.com/").append("guilds/").append(this.server.getId()).append('/').append("users/").append(this.user.getId()).append('/').append("avatars/").append(this.avatarHash).append(this.avatarHash.startsWith("a_") ? ".gif" : ".png").append("?size=").append(size);
            try {
                return Optional.of(new IconImpl(this.api, new URL(url.toString())));
            }
            catch (MalformedURLException e) {
                throw new AssertionError((Object)"Found a malformed role icon url. Please update to the latest Javacord version or create an issue on GitHub if you are already using the latest one.");
            }
        }
        return Optional.empty();
    }

    @Override
    public Instant getJoinedAtTimestamp() {
        return OffsetDateTime.parse(this.joinedAt).toInstant();
    }

    @Override
    public Optional<Instant> getServerBoostingSinceTimestamp() {
        return Optional.ofNullable(this.serverBoostingSince).map(OffsetDateTime::parse).map(OffsetDateTime::toInstant);
    }

    @Override
    public boolean isMuted() {
        return this.muted;
    }

    @Override
    public boolean isDeafened() {
        return this.deafened;
    }

    @Override
    public boolean isSelfMuted() {
        return this.selfMuted;
    }

    @Override
    public boolean isSelfDeafened() {
        return this.selfDeafened;
    }

    @Override
    public boolean isPending() {
        return this.pending;
    }

    @Override
    public Optional<Instant> getTimeout() {
        return Optional.ofNullable(this.communicationDisabledUntil);
    }

    public String toString() {
        return String.format("Member (id: %s, display name: %s)", this.getIdAsString(), this.getDisplayName());
    }
}

