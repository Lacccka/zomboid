/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.emoji;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiUpdaterDelegate;
import org.javacord.api.entity.permission.Role;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class CustomEmojiUpdaterDelegateImpl
implements CustomEmojiUpdaterDelegate {
    private final KnownCustomEmoji emoji;
    private String reason = null;
    private String name = null;
    private Set<Role> whitelist = null;
    private boolean updateWhitelist = false;

    public CustomEmojiUpdaterDelegateImpl(KnownCustomEmoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public void setAuditLogReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addRoleToWhitelist(Role role) {
        if (this.whitelist == null) {
            this.whitelist = this.emoji.getWhitelistedRoles().map(HashSet::new).orElseGet(HashSet::new);
        }
        this.updateWhitelist = true;
        this.whitelist.add(role);
    }

    @Override
    public void removeRoleFromWhitelist(Role role) {
        if (this.whitelist == null) {
            this.whitelist = this.emoji.getWhitelistedRoles().map(HashSet::new).orElseGet(HashSet::new);
        }
        this.updateWhitelist = true;
        this.whitelist.remove(role);
    }

    @Override
    public void removeWhitelist() {
        this.updateWhitelist = true;
        this.whitelist = null;
    }

    @Override
    public void setWhitelist(Collection<Role> roles) {
        this.updateWhitelist = true;
        this.whitelist = roles == null ? null : new HashSet<Role>(roles);
    }

    @Override
    public void setWhitelist(Role ... roles) {
        this.setWhitelist(roles == null ? null : Arrays.asList(roles));
    }

    @Override
    public CompletableFuture<Void> update() {
        boolean patchEmoji = false;
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.name != null) {
            body.put("name", this.name);
            patchEmoji = true;
        }
        if (this.updateWhitelist) {
            if (this.whitelist == null) {
                body.putNull("roles");
            } else {
                ArrayNode jsonRoles = body.putArray("roles");
                this.whitelist.stream().map(DiscordEntity::getIdAsString).forEach(jsonRoles::add);
            }
            patchEmoji = true;
        }
        if (patchEmoji) {
            return new RestRequest(this.emoji.getApi(), RestMethod.PATCH, RestEndpoint.CUSTOM_EMOJI).setUrlParameters(this.emoji.getServer().getIdAsString(), this.emoji.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }
}

