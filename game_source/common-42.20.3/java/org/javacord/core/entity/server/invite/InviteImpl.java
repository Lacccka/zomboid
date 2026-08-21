/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server.invite;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.server.invite.TargetUserType;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class InviteImpl
implements RichInvite {
    private static final Logger logger = LoggerUtil.getLogger(InviteImpl.class);
    private final DiscordApi api;
    private final String code;
    private final long serverId;
    private final String serverName;
    private final String serverIcon;
    private final String serverSplash;
    private final long channelId;
    private final String channelName;
    private final ChannelType channelType;
    private final User inviter;
    private final User targetUser;
    private final TargetUserType targetUserType;
    private final int uses;
    private final int maxUses;
    private final int maxAge;
    private final boolean temporary;
    private final Instant creationTimestamp;
    private final boolean revoked;
    private final Integer approximateMemberCount;
    private final Integer approximatePresenceCount;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public InviteImpl(DiscordApi api, JsonNode data) {
        this.api = api;
        this.code = data.get("code").asText();
        if (data.has("guild")) {
            this.serverId = Long.parseLong(data.get("guild").get("id").asText());
            this.serverName = data.get("guild").get("name").asText();
            this.serverIcon = data.get("guild").has("icon") && !data.get("guild").get("icon").isNull() ? data.get("guild").get("icon").asText() : null;
            this.serverSplash = data.get("guild").has("splash") && !data.get("guild").get("splash").isNull() ? data.get("guild").get("splash").asText() : null;
        } else {
            if (!data.has("guild_id")) throw new AssertionError((Object)"Invite has no guild_id or guild object!");
            this.serverId = Long.parseLong(data.get("guild_id").asText());
            Optional<Server> serverOptional = api.getServerById(this.serverId);
            if (!serverOptional.isPresent()) throw new AssertionError((Object)"Received invite for unknown server!");
            ServerImpl server = (ServerImpl)serverOptional.get();
            this.serverName = server.getName();
            this.serverIcon = server.getIconHash();
            this.serverSplash = server.getSplashHash();
        }
        if (data.has("channel")) {
            this.channelId = Long.parseLong(data.get("channel").get("id").asText());
            this.channelName = data.get("channel").get("name").asText();
            this.channelType = ChannelType.fromId(data.get("channel").get("type").asInt());
        } else {
            if (!data.has("channel_id")) throw new AssertionError((Object)"Invite has no channel_id or channel object!");
            this.channelId = Long.parseLong(data.get("channel_id").asText());
            Optional<ServerChannel> channelOptional = api.getServerChannelById(this.channelId);
            if (!channelOptional.isPresent()) throw new AssertionError((Object)"Received invite for unknown channel!");
            ServerChannel channel = channelOptional.get();
            this.channelName = channel.getName();
            this.channelType = channel.getType();
        }
        this.approximateMemberCount = data.has("approximate_member_count") ? Integer.valueOf(data.get("approximate_member_count").asInt()) : null;
        this.approximatePresenceCount = data.has("approximate_presence_count") ? Integer.valueOf(data.get("approximate_presence_count").asInt()) : null;
        MemberImpl targetMember = null;
        this.targetUser = data.has("target_user") ? new UserImpl((DiscordApiImpl)api, data.get("inviter"), targetMember, (ServerImpl)this.getServer().map(ServerImpl.class::cast).orElse(null)) : null;
        this.targetUserType = data.has("target_user_type") ? TargetUserType.fromId(data.get("target_user_type").asInt()) : null;
        MemberImpl member = null;
        this.inviter = data.has("inviter") ? new UserImpl((DiscordApiImpl)api, data.get("inviter"), member, (ServerImpl)this.getServer().map(ServerImpl.class::cast).orElse(null)) : null;
        this.uses = data.has("uses") ? data.get("uses").asInt() : -1;
        this.maxUses = data.has("max_uses") ? data.get("max_uses").asInt() : -1;
        this.maxAge = data.has("max_age") ? data.get("max_age").asInt() : -1;
        this.temporary = data.has("temporary") && data.get("temporary").asBoolean();
        this.creationTimestamp = data.has("created_at") ? OffsetDateTime.parse(data.get("created_at").asText()).toInstant() : null;
        this.revoked = data.has("revoked") && data.get("revoked").asBoolean();
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public Optional<Server> getServer() {
        return this.api.getServerById(this.serverId);
    }

    @Override
    public long getServerId() {
        return this.serverId;
    }

    @Override
    public String getServerName() {
        return this.serverName;
    }

    @Override
    public Optional<Icon> getServerIcon() {
        if (this.serverIcon == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.api, new URL("https://cdn.discordapp.com/icons/" + Long.toUnsignedString(this.getServerId()) + "/" + this.serverIcon + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Icon> getServerSplash() {
        if (this.serverSplash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.api, new URL("https://cdn.discordapp.com/splashes/" + Long.toUnsignedString(this.getServerId()) + "/" + this.serverSplash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ServerChannel> getChannel() {
        return this.api.getServerChannelById(this.channelId);
    }

    @Override
    public long getChannelId() {
        return this.channelId;
    }

    @Override
    public String getChannelName() {
        return this.channelName;
    }

    @Override
    public ChannelType getChannelType() {
        return this.channelType;
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.INVITE).setUrlParameters(this.getCode()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public Optional<User> getInviter() {
        return Optional.ofNullable(this.inviter);
    }

    @Override
    public Optional<User> getTargetUser() {
        return Optional.ofNullable(this.targetUser);
    }

    @Override
    public Optional<TargetUserType> getTargetUserType() {
        return Optional.ofNullable(this.targetUserType);
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public int getMaxUses() {
        return this.maxUses;
    }

    @Override
    public int getMaxAgeInSeconds() {
        return this.maxAge;
    }

    @Override
    public boolean isTemporary() {
        return this.temporary;
    }

    @Override
    public Instant getCreationTimestamp() {
        return this.creationTimestamp;
    }

    @Override
    public boolean isRevoked() {
        return this.revoked;
    }

    @Override
    public Optional<Integer> getApproximateMemberCount() {
        return Optional.ofNullable(this.approximateMemberCount);
    }

    @Override
    public Optional<Integer> getApproximatePresenceCount() {
        return Optional.ofNullable(this.approximatePresenceCount);
    }
}

