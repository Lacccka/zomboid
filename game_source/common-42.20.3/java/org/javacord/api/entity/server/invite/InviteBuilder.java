/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.internal.InviteBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class InviteBuilder {
    private final InviteBuilderDelegate delegate;

    public InviteBuilder(ServerChannel channel) {
        this.delegate = DelegateFactory.createInviteBuilderDelegate(channel);
    }

    public InviteBuilder setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public InviteBuilder setMaxAgeInSeconds(int maxAge) {
        this.delegate.setMaxAgeInSeconds(maxAge);
        return this;
    }

    public InviteBuilder setNeverExpire() {
        this.delegate.setNeverExpire();
        return this;
    }

    public InviteBuilder setMaxUses(int maxUses) {
        this.delegate.setMaxUses(maxUses);
        return this;
    }

    public InviteBuilder setTemporary(boolean temporary) {
        this.delegate.setTemporary(temporary);
        return this;
    }

    public InviteBuilder setUnique(boolean unique) {
        this.delegate.setUnique(unique);
        return this;
    }

    public CompletableFuture<Invite> create() {
        return this.delegate.create();
    }
}

