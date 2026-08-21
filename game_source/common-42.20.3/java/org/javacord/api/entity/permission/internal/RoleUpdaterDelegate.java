/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission.internal;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Permissions;

public interface RoleUpdaterDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void setPermissions(Permissions var1);

    public void setColor(Color var1);

    public void setDisplaySeparatelyFlag(boolean var1);

    public void setMentionableFlag(boolean var1);

    public CompletableFuture<Void> update();
}

