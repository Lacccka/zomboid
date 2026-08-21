/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannelUpdater;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.user.User;

public interface RegularServerChannel
extends ServerChannel,
Comparable<RegularServerChannel> {
    public int getRawPosition();

    default public int getPosition() {
        return this.getServer().getChannels().indexOf(this);
    }

    default public CompletableFuture<Void> updateRawPosition(int rawPosition) {
        return ((ServerChannelUpdater)this.createUpdater().setRawPosition(rawPosition)).update();
    }

    default public boolean canCreateInstantInvite(User user) {
        if (!this.canSee(user)) {
            return false;
        }
        if (this.getType() == ChannelType.CHANNEL_CATEGORY) {
            return false;
        }
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.CREATE_INSTANT_INVITE);
    }

    default public boolean canYouCreateInstantInvite() {
        return this.canCreateInstantInvite(this.getApi().getYourself());
    }

    public <T extends Permissionable & DiscordEntity> Permissions getOverwrittenPermissions(T var1);

    default public Map<Long, Permissions> getOverwrittenPermissions() {
        HashMap<Long, Permissions> result = new HashMap<Long, Permissions>();
        result.putAll(this.getOverwrittenRolePermissions());
        result.putAll(this.getOverwrittenUserPermissions());
        return Collections.unmodifiableMap(result);
    }

    public Map<Long, Permissions> getOverwrittenUserPermissions();

    public Map<Long, Permissions> getOverwrittenRolePermissions();

    public Permissions getEffectiveOverwrittenPermissions(User var1);

    default public Permissions getEffectivePermissions(User user) {
        if (this.getServer().isOwner(user)) {
            return this.getServer().getPermissions(user);
        }
        PermissionsBuilder builder = new PermissionsBuilder(this.getServer().getPermissions(user));
        Permissions effectiveOverwrittenPermissions = this.getEffectiveOverwrittenPermissions(user);
        Arrays.stream(PermissionType.values()).filter(type -> effectiveOverwrittenPermissions.getState((PermissionType)((Object)type)) != PermissionState.UNSET).forEachOrdered(type -> builder.setState((PermissionType)((Object)type), effectiveOverwrittenPermissions.getState((PermissionType)((Object)type))));
        Arrays.stream(PermissionType.values()).filter(type -> builder.getState((PermissionType)((Object)type)) == PermissionState.UNSET).forEachOrdered(type -> builder.setState((PermissionType)((Object)type), PermissionState.DENIED));
        return builder.build();
    }

    default public Set<PermissionType> getEffectiveAllowedPermissions(User user) {
        return this.getEffectivePermissions(user).getAllowedPermission();
    }

    default public Set<PermissionType> getEffectiveDeniedPermissions(User user) {
        return this.getEffectivePermissions(user).getDeniedPermissions();
    }

    default public boolean hasPermissions(User user, PermissionType ... type) {
        return this.getEffectiveAllowedPermissions(user).containsAll(Arrays.asList(type));
    }

    default public boolean hasAnyPermission(User user, PermissionType ... type) {
        return this.getEffectiveAllowedPermissions(user).stream().anyMatch(allowedPermissionType -> Arrays.stream(type).anyMatch(allowedPermissionType::equals));
    }

    default public boolean hasPermission(User user, PermissionType permission) {
        return this.getEffectiveAllowedPermissions(user).contains((Object)permission);
    }

    @Override
    default public int compareTo(RegularServerChannel channel) {
        if (!this.getServer().equals(channel.getServer())) {
            throw new IllegalArgumentException("Only channels from the same server can be compared for order");
        }
        return this.getPosition() - channel.getPosition();
    }

    @Override
    default public RegularServerChannelUpdater createUpdater() {
        return new RegularServerChannelUpdater(this);
    }

    default public Optional<? extends RegularServerChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getChannelById(this.getId())).flatMap(Channel::asRegularServerChannel);
    }

    @Override
    default public CompletableFuture<? extends RegularServerChannel> getLatestInstance() {
        Optional<? extends RegularServerChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

