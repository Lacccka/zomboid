/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.emoji;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.emoji.CustomEmojiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.listener.server.emoji.InternalKnownCustomEmojiAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class KnownCustomEmojiImpl
extends CustomEmojiImpl
implements KnownCustomEmoji,
InternalKnownCustomEmojiAttachableListenerManager {
    private final Server server;
    private volatile Collection<Role> whitelist;
    private final boolean requiresColons;
    private final boolean managed;
    private volatile long creatorId;

    public KnownCustomEmojiImpl(DiscordApiImpl api, Server server, JsonNode data) {
        super(api, data);
        this.server = server;
        if (data.hasNonNull("roles")) {
            this.whitelist = new HashSet<Role>();
            for (JsonNode roleIdJson : data.get("roles")) {
                server.getRoleById(roleIdJson.asLong()).ifPresent(this.whitelist::add);
            }
        }
        this.requiresColons = !data.hasNonNull("require_colons") || data.get("require_colons").asBoolean();
        this.creatorId = data.has("user") ? data.get("user").asLong() : 0L;
        this.managed = data.get("managed").asBoolean(false);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWhitelist(Collection<Role> whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.CUSTOM_EMOJI).setUrlParameters(this.getServer().getIdAsString(), this.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public Optional<Set<Role>> getWhitelistedRoles() {
        return this.whitelist == null || this.whitelist.isEmpty() ? Optional.empty() : Optional.of(Collections.unmodifiableSet(new HashSet<Role>(this.whitelist)));
    }

    @Override
    public boolean requiresColons() {
        return this.requiresColons;
    }

    @Override
    public boolean isManaged() {
        return this.managed;
    }

    @Override
    public CompletableFuture<Optional<User>> getCreator() {
        if (this.creatorId == 0L) {
            return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.CUSTOM_EMOJI).setUrlParameters(this.server.getIdAsString(), this.getIdAsString()).execute(result -> {
                JsonNode userJson = result.getJsonBody().get("user");
                if (userJson.isMissingNode()) {
                    return Optional.empty();
                }
                this.creatorId = userJson.get("id").asLong();
                return Optional.of(new UserImpl((DiscordApiImpl)this.getApi(), userJson, (MemberImpl)null, (ServerImpl)this.server));
            });
        }
        return this.getApi().getUserById(this.creatorId).thenApply(Optional::of);
    }

    @Override
    public String toString() {
        return String.format("KnownCustomEmoji (id: %s, name: %s, animated: %b, server: %#s)", this.getIdAsString(), this.getName(), this.isAnimated(), this.getServer());
    }
}

