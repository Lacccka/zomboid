/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server.invite;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.internal.InviteBuilderDelegate;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class InviteBuilderDelegateImpl
implements InviteBuilderDelegate {
    private final ServerChannel channel;
    private String reason = null;
    private int maxAge = 86400;
    private int maxUses = 0;
    private boolean temporary = false;
    private boolean unique = false;

    public InviteBuilderDelegateImpl(ServerChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setAuditLogReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void setMaxAgeInSeconds(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public void setNeverExpire() {
        this.setMaxAgeInSeconds(0);
    }

    @Override
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    @Override
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    @Override
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public CompletableFuture<Invite> create() {
        return new RestRequest(this.channel.getApi(), RestMethod.POST, RestEndpoint.CHANNEL_INVITE).setUrlParameters(this.channel.getIdAsString()).setBody(JsonNodeFactory.instance.objectNode().put("max_age", this.maxAge).put("max_uses", this.maxUses).put("temporary", this.temporary).put("unique", this.unique)).setAuditLogReason(this.reason).execute(result -> new InviteImpl(this.channel.getApi(), result.getJsonBody()));
    }
}

