/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.permission;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.internal.RoleUpdaterDelegate;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class RoleUpdaterDelegateImpl
implements RoleUpdaterDelegate {
    private final Role role;
    private String reason = null;
    private String name = null;
    private Permissions permissions = null;
    private Color color = null;
    private Boolean displaySeparately = null;
    private Boolean mentionable = null;

    public RoleUpdaterDelegateImpl(Role role) {
        this.role = role;
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
    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setDisplaySeparatelyFlag(boolean displaySeparately) {
        this.displaySeparately = displaySeparately;
    }

    @Override
    public void setMentionableFlag(boolean mentionable) {
        this.mentionable = mentionable;
    }

    @Override
    public CompletableFuture<Void> update() {
        boolean patchRole = false;
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.name != null) {
            body.put("name", this.name);
            patchRole = true;
        }
        if (this.permissions != null) {
            body.put("permissions", this.permissions.getAllowedBitmask());
            patchRole = true;
        }
        if (this.color != null) {
            body.put("color", this.color.getRGB() & 0xFFFFFF);
            patchRole = true;
        }
        if (this.displaySeparately != null) {
            body.put("hoist", (boolean)this.displaySeparately);
            patchRole = true;
        }
        if (this.mentionable != null) {
            body.put("mentionable", (boolean)this.mentionable);
            patchRole = true;
        }
        if (patchRole) {
            return new RestRequest(this.role.getApi(), RestMethod.PATCH, RestEndpoint.ROLE).setUrlParameters(this.role.getServer().getIdAsString(), this.role.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }
}

