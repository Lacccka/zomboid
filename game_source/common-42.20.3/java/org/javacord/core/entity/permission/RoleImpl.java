/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.permission;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleTags;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.permission.PermissionsImpl;
import org.javacord.core.entity.permission.RoleTagsImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.listener.server.role.InternalRoleAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class RoleImpl
implements Role,
InternalRoleAttachableListenerManager {
    private static final Comparator<Role> ROLE_COMPARATOR = Comparator.comparingInt(Role::getRawPosition).thenComparing(Comparator.comparing(DiscordEntity::getId).reversed());
    private static final int DEFAULT_ICON_SIZE = 1024;
    private final DiscordApiImpl api;
    private final ServerImpl server;
    private final RoleTagsImpl roleTags;
    private final long id;
    private volatile String name;
    private volatile int rawPosition;
    private volatile int color;
    private volatile boolean hoist;
    private final String iconHash;
    private final String unicodeEmoji;
    private volatile boolean mentionable;
    private volatile PermissionsImpl permissions;
    private final boolean managed;

    public RoleImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        this.api = api;
        this.server = server;
        this.id = data.get("id").asLong();
        this.name = data.get("name").asText();
        this.rawPosition = data.get("position").asInt();
        this.color = data.get("color").asInt(0);
        this.hoist = data.get("hoist").asBoolean(false);
        this.iconHash = data.hasNonNull("icon") ? data.get("icon").asText() : null;
        this.unicodeEmoji = data.hasNonNull("unicode_emoji") ? data.get("unicode_emoji").asText() : null;
        this.mentionable = data.get("mentionable").asBoolean(false);
        this.permissions = new PermissionsImpl(data.get("permissions").asLong(), 0L);
        this.managed = data.get("managed").asBoolean(false);
        this.roleTags = data.has("tags") ? new RoleTagsImpl(data.get("tags")) : null;
    }

    public int getColorAsInt() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setHoist(boolean hoist) {
        this.hoist = hoist;
    }

    public void setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(PermissionsImpl permissions) {
        this.permissions = permissions;
    }

    public void setRawPosition(int position) {
        this.rawPosition = position;
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
    public Server getServer() {
        return this.server;
    }

    @Override
    public Optional<RoleTags> getRoleTags() {
        return Optional.ofNullable(this.roleTags);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getRawPosition() {
        return this.rawPosition;
    }

    @Override
    public Optional<String> getIconHash() {
        return Optional.ofNullable(this.iconHash);
    }

    @Override
    public Optional<Icon> getIcon() {
        return this.getIcon(1024);
    }

    @Override
    public Optional<Icon> getIcon(int size) {
        if (this.iconHash != null) {
            StringBuilder url = new StringBuilder("https://cdn.discordapp.com/").append("role-icons/").append(this.id).append('/').append(this.iconHash).append(this.iconHash.startsWith("a_") ? ".gif" : ".png").append("?size=").append(size);
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
    public Optional<String> getUnicodeEmojiIcon() {
        return Optional.ofNullable(this.unicodeEmoji);
    }

    @Override
    public Optional<Color> getColor() {
        return Optional.ofNullable(this.color == 0 ? null : new Color(this.color));
    }

    @Override
    public boolean isMentionable() {
        return this.mentionable;
    }

    @Override
    public boolean isDisplayedSeparately() {
        return this.hoist;
    }

    @Override
    public Set<User> getUsers() {
        if (this.isEveryoneRole()) {
            return this.getServer().getMembers();
        }
        return this.server.getRealMembers().stream().filter(member -> member.hasRole(this)).map(Member::getUser).collect(Collectors.toSet());
    }

    @Override
    public boolean hasUser(User user) {
        return ((UserImpl)user).getMember().filter(member -> member.getServer().equals(this.server)).map(Member.class::cast).map(Optional::of).orElseGet(() -> this.server.getRealMemberById(user.getId())).map(member -> member.hasRole(this)).orElse(false);
    }

    @Override
    public Permissions getPermissions() {
        return this.permissions;
    }

    @Override
    public boolean isManaged() {
        return this.managed;
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.ROLE).setAuditLogReason(reason).setUrlParameters(this.getServer().getIdAsString(), this.getIdAsString()).execute(result -> null);
    }

    @Override
    public int compareTo(Role role) {
        if (!role.getServer().equals(this.getServer())) {
            throw new IllegalArgumentException("Only roles from the same server can be compared for order");
        }
        return ROLE_COMPARATOR.compare(this, role);
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("Role (id: %s, name: %s, server: %#s)", this.getIdAsString(), this.getName(), this.getServer());
    }
}

