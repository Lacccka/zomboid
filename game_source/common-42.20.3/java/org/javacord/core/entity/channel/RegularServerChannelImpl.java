/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.ServerChannelImpl;
import org.javacord.core.entity.permission.PermissionsImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class RegularServerChannelImpl
extends ServerChannelImpl
implements RegularServerChannel {
    public static final Comparator<RegularServerChannel> COMPARE_BY_RAW_POSITION = Comparator.comparingInt(RegularServerChannel::getRawPosition).thenComparingLong(DiscordEntity::getId);
    private final ConcurrentHashMap<Long, Permissions> overwrittenUserPermissions = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, Permissions> overwrittenRolePermissions = new ConcurrentHashMap();
    private volatile int rawPosition;

    public RegularServerChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.rawPosition = data.get("position").asInt();
        if (data.has("permission_overwrites")) {
            block4: for (JsonNode permissionOverwrite : data.get("permission_overwrites")) {
                long id = Long.parseLong(permissionOverwrite.has("id") ? permissionOverwrite.get("id").asText() : "-1");
                long allow = permissionOverwrite.has("allow") ? permissionOverwrite.get("allow").asLong() : 0L;
                long deny = permissionOverwrite.has("deny") ? permissionOverwrite.get("deny").asLong() : 0L;
                PermissionsImpl permissions = new PermissionsImpl(allow, deny);
                switch (permissionOverwrite.get("type").asInt()) {
                    case 0: {
                        this.overwrittenRolePermissions.put(id, permissions);
                        continue block4;
                    }
                    case 1: {
                        this.overwrittenUserPermissions.put(id, permissions);
                        continue block4;
                    }
                }
                LoggerUtil.getLogger(ServerChannelImpl.class).warn("Unknown type for permission_overwrites. Your Javacord version might be outdated.");
            }
        }
    }

    public ConcurrentHashMap<Long, Permissions> getInternalOverwrittenRolePermissions() {
        return this.overwrittenRolePermissions;
    }

    public ConcurrentHashMap<Long, Permissions> getInternalOverwrittenUserPermissions() {
        return this.overwrittenUserPermissions;
    }

    @Override
    public <T extends Permissionable & DiscordEntity> Permissions getOverwrittenPermissions(T permissionable) {
        Map permissionsMap = Collections.emptyMap();
        if (permissionable instanceof User) {
            permissionsMap = this.overwrittenUserPermissions;
        } else if (permissionable instanceof Role) {
            permissionsMap = this.overwrittenRolePermissions;
        }
        return permissionsMap.getOrDefault(((DiscordEntity)permissionable).getId(), PermissionsImpl.EMPTY_PERMISSIONS);
    }

    @Override
    public Map<Long, Permissions> getOverwrittenUserPermissions() {
        return Collections.unmodifiableMap(new HashMap<Long, Permissions>(this.overwrittenUserPermissions));
    }

    @Override
    public Map<Long, Permissions> getOverwrittenRolePermissions() {
        return Collections.unmodifiableMap(new HashMap<Long, Permissions>(this.overwrittenRolePermissions));
    }

    @Override
    public Permissions getEffectiveOverwrittenPermissions(User user) {
        PermissionsBuilder builder = new PermissionsBuilder(PermissionsImpl.EMPTY_PERMISSIONS);
        Server server = this.getServer();
        Role everyoneRole = server.getEveryoneRole();
        Permissions everyoneRolePermissionOverwrites = this.getOverwrittenPermissions(everyoneRole);
        for (PermissionType type : PermissionType.values()) {
            if (everyoneRolePermissionOverwrites.getState(type) == PermissionState.DENIED) {
                builder.setState(type, PermissionState.DENIED);
            }
            if (everyoneRolePermissionOverwrites.getState(type) != PermissionState.ALLOWED) continue;
            builder.setState(type, PermissionState.ALLOWED);
        }
        ArrayList<Role> rolesOfUser = new ArrayList<Role>(server.getRoles(user));
        rolesOfUser.remove(everyoneRole);
        List permissionOverwrites = rolesOfUser.stream().map(this::getOverwrittenPermissions).collect(Collectors.toList());
        for (Permissions permissions : permissionOverwrites) {
            for (PermissionType type : PermissionType.values()) {
                if (permissions.getState(type) != PermissionState.DENIED) continue;
                builder.setState(type, PermissionState.DENIED);
            }
        }
        for (Permissions permissions : permissionOverwrites) {
            for (PermissionType type : PermissionType.values()) {
                if (permissions.getState(type) != PermissionState.ALLOWED) continue;
                builder.setState(type, PermissionState.ALLOWED);
            }
        }
        for (PermissionType type : PermissionType.values()) {
            Permissions permissions = this.overwrittenUserPermissions.getOrDefault(user.getId(), PermissionsImpl.EMPTY_PERMISSIONS);
            if (permissions.getState(type) == PermissionState.DENIED) {
                builder.setState(type, PermissionState.DENIED);
            }
            if (permissions.getState(type) != PermissionState.ALLOWED) continue;
            builder.setState(type, PermissionState.ALLOWED);
        }
        return builder.build();
    }

    @Override
    public int getRawPosition() {
        return this.rawPosition;
    }

    public void setRawPosition(int position) {
        this.rawPosition = position;
    }
}

