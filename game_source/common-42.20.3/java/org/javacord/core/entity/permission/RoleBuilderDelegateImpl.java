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
import org.javacord.api.entity.permission.internal.RoleBuilderDelegate;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class RoleBuilderDelegateImpl
implements RoleBuilderDelegate {
    private final ServerImpl server;
    private String reason = null;
    private String name = null;
    private Permissions permissions = null;
    private Color color = null;
    private boolean mentionable = false;
    private boolean displaySeparately = false;

    public RoleBuilderDelegateImpl(ServerImpl server) {
        this.server = server;
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
    public void setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
    }

    @Override
    public void setDisplaySeparately(boolean displaySeparately) {
        this.displaySeparately = displaySeparately;
    }

    @Override
    public CompletableFuture<Role> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.name != null) {
            body.put("name", this.name);
        }
        if (this.permissions != null) {
            body.put("permissions", this.permissions.getAllowedBitmask());
        }
        if (this.color != null) {
            body.put("color", this.color.getRGB() & 0xFFFFFF);
        }
        body.put("mentionable", this.mentionable);
        body.put("hoist", this.displaySeparately);
        return new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.ROLE).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> this.server.getOrCreateRole(result.getJsonBody()));
    }
}

