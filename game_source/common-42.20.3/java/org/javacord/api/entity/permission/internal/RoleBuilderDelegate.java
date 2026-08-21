/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission.internal;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;

public interface RoleBuilderDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void setPermissions(Permissions var1);

    public void setColor(Color var1);

    public void setMentionable(boolean var1);

    public void setDisplaySeparately(boolean var1);

    public CompletableFuture<Role> create();
}

