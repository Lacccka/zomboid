/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji.internal;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Role;

public interface CustomEmojiUpdaterDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public void addRoleToWhitelist(Role var1);

    public void removeRoleFromWhitelist(Role var1);

    public void removeWhitelist();

    public void setWhitelist(Collection<Role> var1);

    public void setWhitelist(Role ... var1);

    public CompletableFuture<Void> update();
}

