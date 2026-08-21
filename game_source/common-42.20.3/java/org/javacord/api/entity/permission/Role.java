/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import java.awt.Color;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.RoleTags;
import org.javacord.api.entity.permission.RoleUpdater;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.server.role.RoleAttachableListenerManager;

public interface Role
extends DiscordEntity,
Mentionable,
Nameable,
Deletable,
Permissionable,
Comparable<Role>,
UpdatableFromCache<Role>,
RoleAttachableListenerManager {
    public Server getServer();

    public Optional<RoleTags> getRoleTags();

    default public int getPosition() {
        return this.getServer().getRoles().indexOf(this);
    }

    public int getRawPosition();

    public Optional<Color> getColor();

    public Optional<String> getIconHash();

    public Optional<Icon> getIcon();

    public Optional<Icon> getIcon(int var1);

    public Optional<String> getUnicodeEmojiIcon();

    public boolean isMentionable();

    public boolean isDisplayedSeparately();

    public Set<User> getUsers();

    public boolean hasUser(User var1);

    public Permissions getPermissions();

    public boolean isManaged();

    default public boolean isEveryoneRole() {
        return this.getId() == this.getServer().getId();
    }

    default public RoleUpdater createUpdater() {
        return new RoleUpdater(this);
    }

    default public CompletableFuture<Void> updateName(String name) {
        return this.createUpdater().setName(name).update();
    }

    default public CompletableFuture<Void> updatePermissions(Permissions permissions) {
        return this.createUpdater().setPermissions(permissions).update();
    }

    default public CompletableFuture<Void> updateColor(Color color) {
        return this.createUpdater().setColor(color).update();
    }

    default public CompletableFuture<Void> updateDisplaySeparatelyFlag(boolean displaySeparately) {
        return this.createUpdater().setDisplaySeparatelyFlag(displaySeparately).update();
    }

    default public CompletableFuture<Void> updateMentionableFlag(boolean mentionable) {
        return this.createUpdater().setMentionableFlag(mentionable).update();
    }

    default public CompletableFuture<Void> addUser(User user) {
        return this.addUser(user, null);
    }

    default public CompletableFuture<Void> addUser(User user, String reason) {
        return this.getServer().addRoleToUser(user, this, reason);
    }

    default public CompletableFuture<Void> removeUser(User user) {
        return this.removeUser(user, null);
    }

    default public CompletableFuture<Void> removeUser(User user, String reason) {
        return this.getServer().removeRoleFromUser(user, this, reason);
    }

    default public Set<PermissionType> getAllowedPermissions() {
        return this.getPermissions().getAllowedPermission();
    }

    default public Set<PermissionType> getUnsetPermissions() {
        return this.getPermissions().getUnsetPermissions();
    }

    @Override
    default public String getMentionTag() {
        return this.isEveryoneRole() ? "@everyone" : "<@&" + this.getIdAsString() + ">";
    }

    @Override
    default public Optional<Role> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getRoleById(this.getId()));
    }
}

