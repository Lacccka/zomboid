/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.user;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.UserFlag;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.listener.user.UserAttachableListenerManager;

public interface User
extends DiscordEntity,
Messageable,
Nameable,
Mentionable,
Permissionable,
UpdatableFromCache<User>,
UserAttachableListenerManager {
    @Override
    default public String getMentionTag() {
        return "<@" + this.getIdAsString() + ">";
    }

    default public String getNicknameMentionTag() {
        return "<@!" + this.getIdAsString() + ">";
    }

    public String getDiscriminator();

    public boolean isBot();

    default public boolean isBotOwner() {
        return this.getApi().getOwnerId().isPresent() && this.getApi().getOwnerId().get().longValue() == this.getId();
    }

    default public boolean isTeamMember() {
        return this.getApi().getCachedTeam().map(team -> team.getOwnerId() == this.getId() || team.getTeamMembers().stream().anyMatch(teamMember -> teamMember.getId() == this.getId())).orElse(false);
    }

    default public boolean isBotOwnerOrTeamMember() {
        return this.isBotOwner() || this.isTeamMember();
    }

    public Set<Activity> getActivities();

    default public boolean canManageRole(Role role) {
        return role.getServer().canManageRole(this, role);
    }

    default public Set<ServerVoiceChannel> getConnectedVoiceChannels() {
        return Collections.unmodifiableSet(this.getApi().getServerVoiceChannels().stream().filter(this::isConnected).collect(Collectors.toSet()));
    }

    default public boolean isConnected(ServerVoiceChannel channel) {
        return channel.isConnected(this.getId());
    }

    default public Optional<ServerVoiceChannel> getConnectedVoiceChannel(Server server) {
        return server.getConnectedVoiceChannel(this.getId());
    }

    public UserStatus getStatus();

    public UserStatus getStatusOnClient(DiscordClient var1);

    default public UserStatus getDesktopStatus() {
        return this.getStatusOnClient(DiscordClient.DESKTOP);
    }

    default public UserStatus getMobileStatus() {
        return this.getStatusOnClient(DiscordClient.MOBILE);
    }

    default public UserStatus getWebStatus() {
        return this.getStatusOnClient(DiscordClient.WEB);
    }

    default public Set<DiscordClient> getCurrentClients() {
        Set connectedClients = Arrays.stream(DiscordClient.values()).filter(client -> this.getStatusOnClient((DiscordClient)((Object)client)) != UserStatus.OFFLINE).collect(Collectors.toSet());
        return Collections.unmodifiableSet(connectedClients);
    }

    public EnumSet<UserFlag> getUserFlags();

    public Optional<String> getAvatarHash();

    public Icon getAvatar();

    public Icon getAvatar(int var1);

    public Optional<String> getServerAvatarHash(Server var1);

    public Optional<Icon> getServerAvatar(Server var1);

    public Optional<Icon> getServerAvatar(Server var1, int var2);

    public Icon getEffectiveAvatar(Server var1);

    public Icon getEffectiveAvatar(Server var1, int var2);

    public boolean hasDefaultAvatar();

    public Set<Server> getMutualServers();

    public String getDisplayName(Server var1);

    default public String getDiscriminatedName() {
        return this.getName() + "#" + this.getDiscriminator();
    }

    default public CompletableFuture<Void> updateNickname(Server server, String nickname) {
        return server.updateNickname(this, nickname);
    }

    default public CompletableFuture<Void> updateNickname(Server server, String nickname, String reason) {
        return server.updateNickname(this, nickname, reason);
    }

    default public CompletableFuture<Void> resetNickname(Server server) {
        return server.resetNickname(this);
    }

    default public CompletableFuture<Void> resetNickname(Server server, String reason) {
        return server.resetNickname(this, reason);
    }

    public Optional<String> getNickname(Server var1);

    public Optional<Instant> getServerBoostingSinceTimestamp(Server var1);

    default public CompletableFuture<Void> timeout(Server server, Instant timeout2) {
        return server.timeoutUser(this, timeout2);
    }

    default public CompletableFuture<Void> timeout(Server server, Instant timeout2, String reason) {
        return server.timeoutUser(this, timeout2, reason);
    }

    default public CompletableFuture<Void> timeout(Server server, Duration duration) {
        return server.timeoutUser(this, Instant.now().plus(duration));
    }

    default public CompletableFuture<Void> timeout(Server server, Duration duration, String reason) {
        return server.timeoutUser(this, Instant.now().plus(duration), reason);
    }

    default public CompletableFuture<Void> removeTimeout(Server server) {
        return server.timeoutUser(this, Instant.MIN);
    }

    default public CompletableFuture<Void> removeTimeout(Server server, String reason) {
        return server.timeoutUser(this, Instant.MIN, reason);
    }

    public Optional<Instant> getTimeout(Server var1);

    default public Optional<Instant> getActiveTimeout(Server server) {
        return this.getTimeout(server).filter(Instant.now()::isBefore);
    }

    public boolean isPending(Server var1);

    default public CompletableFuture<Void> move(ServerVoiceChannel channel) {
        return channel.getServer().moveUser(this, channel);
    }

    public boolean isSelfMuted(Server var1);

    public boolean isSelfDeafened(Server var1);

    default public CompletableFuture<Void> mute(Server server) {
        return server.muteUser(this);
    }

    default public CompletableFuture<Void> mute(Server server, String reason) {
        return server.muteUser(this, reason);
    }

    default public CompletableFuture<Void> unmute(Server server) {
        return server.unmuteUser(this);
    }

    default public CompletableFuture<Void> unmute(Server server, String reason) {
        return server.unmuteUser(this, reason);
    }

    default public boolean isMuted(Server server) {
        return server.isMuted(this.getId());
    }

    default public CompletableFuture<Void> deafen(Server server) {
        return server.deafenUser(this);
    }

    default public CompletableFuture<Void> deafen(Server server, String reason) {
        return server.deafenUser(this, reason);
    }

    default public CompletableFuture<Void> undeafen(Server server) {
        return server.undeafenUser(this);
    }

    default public CompletableFuture<Void> undeafen(Server server, String reason) {
        return server.undeafenUser(this, reason);
    }

    default public boolean isDeafened(Server server) {
        return server.isDeafened(this.getId());
    }

    public Optional<Instant> getJoinedAtTimestamp(Server var1);

    public List<Role> getRoles(Server var1);

    public Optional<Color> getRoleColor(Server var1);

    default public boolean isYourself() {
        return this.getId() == this.getApi().getYourself().getId();
    }

    public Optional<PrivateChannel> getPrivateChannel();

    public CompletableFuture<PrivateChannel> openPrivateChannel();

    default public CompletableFuture<Void> addRole(Role role) {
        return this.addRole(role, null);
    }

    default public CompletableFuture<Void> addRole(Role role, String reason) {
        return role.getServer().addRoleToUser(this, role, reason);
    }

    default public CompletableFuture<Void> removeRole(Role role) {
        return this.removeRole(role, null);
    }

    default public CompletableFuture<Void> removeRole(Role role, String reason) {
        return role.getServer().removeRoleFromUser(this, role, reason);
    }

    @Override
    default public Optional<User> getCurrentCachedInstance() {
        return this.getApi().getCachedUserById(this.getId());
    }

    @Override
    default public CompletableFuture<User> getLatestInstance() {
        return this.getApi().getUserById(this.getId());
    }
}

