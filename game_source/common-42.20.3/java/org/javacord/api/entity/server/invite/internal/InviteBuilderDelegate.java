/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.server.invite.Invite;

public interface InviteBuilderDelegate {
    public void setAuditLogReason(String var1);

    public void setMaxAgeInSeconds(int var1);

    public void setNeverExpire();

    public void setMaxUses(int var1);

    public void setTemporary(boolean var1);

    public void setUnique(boolean var1);

    public CompletableFuture<Invite> create();
}

