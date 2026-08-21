/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserFlag;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserPresence;
import org.javacord.core.listener.user.InternalUserAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class UserImpl
implements User,
InternalUserAttachableListenerManager {
    private static final int DEFAULT_AVATAR_SIZE = 1024;
    private final DiscordApiImpl api;
    private final Long id;
    private final String name;
    private final String discriminator;
    private final String avatarHash;
    private EnumSet<UserFlag> userFlags = EnumSet.noneOf(UserFlag.class);
    private final boolean bot;
    private final MemberImpl member;

    public UserImpl(DiscordApiImpl api, JsonNode data, MemberImpl member, ServerImpl server) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.name = data.get("username").asText();
        this.discriminator = data.get("discriminator").asText();
        this.avatarHash = data.hasNonNull("avatar") ? data.get("avatar").asText() : null;
        if (data.has("public_flags")) {
            int flags = data.get("public_flags").asInt();
            for (UserFlag flag : UserFlag.values()) {
                if ((flag.asInt() & flags) != flag.asInt()) continue;
                this.userFlags.add(flag);
            }
        }
        this.bot = data.hasNonNull("bot") && data.get("bot").asBoolean();
        this.member = member == null && data.hasNonNull("member") && server != null ? new MemberImpl(api, server, data.get("member"), this) : member;
    }

    public UserImpl(DiscordApiImpl api, JsonNode data, JsonNode memberJson, ServerImpl server) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.name = data.get("username").asText();
        this.discriminator = data.get("discriminator").asText();
        this.avatarHash = data.hasNonNull("avatar") ? data.get("avatar").asText() : null;
        if (data.has("public_flags")) {
            int flags = data.get("public_flags").asInt();
            for (UserFlag flag : UserFlag.values()) {
                if ((flag.asInt() & flags) != flag.asInt()) continue;
                this.userFlags.add(flag);
            }
        }
        this.bot = data.hasNonNull("bot") && data.get("bot").asBoolean();
        this.member = new MemberImpl(api, server, memberJson, this);
    }

    private UserImpl(DiscordApiImpl api, Long id, String name, String discriminator, String avatarHash, boolean bot, MemberImpl member) {
        this.api = api;
        this.id = id;
        this.name = name;
        this.discriminator = discriminator;
        this.avatarHash = avatarHash;
        this.bot = bot;
        this.member = member;
    }

    public UserImpl replacePartialUserData(JsonNode partialUserJson) {
        if (partialUserJson.get("id").asLong() != this.id.longValue()) {
            throw new IllegalArgumentException("Ids of user do not match");
        }
        String name = this.name;
        if (partialUserJson.hasNonNull("username")) {
            name = partialUserJson.get("username").asText();
        }
        String discriminator = this.discriminator;
        if (partialUserJson.hasNonNull("discriminator")) {
            discriminator = partialUserJson.get("discriminator").asText();
        }
        String avatarHash = this.avatarHash;
        if (partialUserJson.hasNonNull("avatar")) {
            avatarHash = partialUserJson.get("avatar").asText();
        }
        return new UserImpl(this.api, this.id, name, discriminator, avatarHash, this.bot, this.member);
    }

    public Optional<MemberImpl> getMember() {
        return Optional.ofNullable(this.member);
    }

    @Override
    public Optional<String> getAvatarHash() {
        return Optional.ofNullable(this.avatarHash);
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
    public String getDiscriminator() {
        return this.discriminator;
    }

    @Override
    public EnumSet<UserFlag> getUserFlags() {
        return this.userFlags;
    }

    public static Icon getAvatar(DiscordApi api, String avatarHash, String discriminator, long userId) {
        return UserImpl.getAvatar(api, avatarHash, discriminator, userId, 1024);
    }

    public static Icon getAvatar(DiscordApi api, String avatarHash, String discriminator, long userId, int size) {
        StringBuilder url = new StringBuilder("https://cdn.discordapp.com/");
        if (avatarHash == null) {
            url.append("embed/avatars/").append(Integer.parseInt(discriminator) % 5).append(".png");
        } else {
            url.append("avatars/").append(userId).append('/').append(avatarHash).append(avatarHash.startsWith("a_") ? ".gif" : ".png");
        }
        url.append("?size=").append(size);
        try {
            return new IconImpl(api, new URL(url.toString()));
        }
        catch (MalformedURLException e) {
            throw new AssertionError((Object)"Found a malformed avatar url. Please update to the latest Javacord version or create an issue on GitHub if you are already using the latest one.");
        }
    }

    @Override
    public Icon getAvatar() {
        return UserImpl.getAvatar(this.api, this.avatarHash, this.discriminator, this.getId());
    }

    @Override
    public Icon getAvatar(int size) {
        return UserImpl.getAvatar(this.api, this.avatarHash, this.discriminator, this.getId(), size);
    }

    @Override
    public Optional<Icon> getServerAvatar(Server server) {
        return this.getServerAvatar(server, 1024);
    }

    @Override
    public Optional<Icon> getServerAvatar(Server server, int size) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getUserServerAvatar(this, size);
        }
        return this.member.getServerAvatar(size);
    }

    @Override
    public Icon getEffectiveAvatar(Server server) {
        return this.getServerAvatar(server).orElse(this.getAvatar());
    }

    @Override
    public Icon getEffectiveAvatar(Server server, int size) {
        return this.getServerAvatar(server, size).orElse(this.getAvatar(size));
    }

    @Override
    public boolean hasDefaultAvatar() {
        return this.avatarHash == null;
    }

    @Override
    public Set<Server> getMutualServers() {
        if (this.api.isUserCacheEnabled()) {
            return this.api.getEntityCache().get().getMemberCache().getServers(this.getId());
        }
        return this.member == null ? Collections.emptySet() : Collections.singleton(this.member.getServer());
    }

    @Override
    public Optional<Instant> getServerBoostingSinceTimestamp(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getServerBoostingSinceTimestamp(this);
        }
        return this.member.getServerBoostingSinceTimestamp();
    }

    @Override
    public String getDisplayName(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getNickname(this).orElseGet(this::getName);
        }
        return this.member.getNickname().orElse(this.getName());
    }

    @Override
    public Optional<String> getNickname(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getNickname(this);
        }
        return this.member.getNickname();
    }

    @Override
    public Optional<Instant> getTimeout(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getTimeout(this);
        }
        return this.member.getTimeout();
    }

    @Override
    public boolean isPending(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.isPending(this.getId());
        }
        return this.member.isPending();
    }

    @Override
    public boolean isSelfMuted(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.isSelfMuted(this.getId());
        }
        return this.member.isSelfMuted();
    }

    @Override
    public boolean isSelfDeafened(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.isSelfDeafened(this.getId());
        }
        return this.member.isSelfDeafened();
    }

    @Override
    public Optional<Instant> getJoinedAtTimestamp(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getJoinedAtTimestamp(this);
        }
        return Optional.of(this.member.getJoinedAtTimestamp());
    }

    @Override
    public List<Role> getRoles(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getRoles(this);
        }
        return this.member.getRoles();
    }

    @Override
    public Optional<Color> getRoleColor(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getRoleColor(this);
        }
        return this.member.getRoleColor();
    }

    @Override
    public Optional<String> getServerAvatarHash(Server server) {
        if (this.api.hasUserCacheEnabled() || this.member == null || this.member.getServer().getId() != server.getId()) {
            return server.getUserServerAvatarHash(this);
        }
        return this.member.getServerAvatarHash();
    }

    @Override
    public boolean isBot() {
        return this.bot;
    }

    @Override
    public Set<Activity> getActivities() {
        return this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(this.getId()).map(UserPresence::getActivities).orElse(Collections.emptySet());
    }

    @Override
    public UserStatus getStatus() {
        return this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(this.getId()).map(UserPresence::getStatus).orElse(UserStatus.OFFLINE);
    }

    @Override
    public UserStatus getStatusOnClient(DiscordClient client) {
        return this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(this.getId()).map(UserPresence::getClientStatus).map(clientStatusMap -> clientStatusMap.getOrElse(client, UserStatus.OFFLINE)).orElse(UserStatus.OFFLINE);
    }

    @Override
    public Optional<PrivateChannel> getPrivateChannel() {
        return this.api.getEntityCache().get().getChannelCache().getPrivateChannelByUserId(this.getId());
    }

    @Override
    public CompletableFuture<PrivateChannel> openPrivateChannel() {
        return this.getPrivateChannel().map(CompletableFuture::completedFuture).orElseGet(() -> new RestRequest(this.api, RestMethod.POST, RestEndpoint.USER_CHANNEL).setBody(JsonNodeFactory.instance.objectNode().put("recipient_id", this.getIdAsString())).execute(result -> new PrivateChannelImpl(this.api, result.getJsonBody())));
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("User (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

