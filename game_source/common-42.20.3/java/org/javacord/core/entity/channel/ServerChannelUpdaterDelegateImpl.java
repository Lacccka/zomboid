/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerChannelUpdaterDelegateImpl
implements ServerChannelUpdaterDelegate {
    protected final ServerChannel channel;
    protected String reason = null;
    protected String name = null;

    public ServerChannelUpdaterDelegateImpl(ServerChannel channel) {
        this.channel = channel;
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
    public CompletableFuture<Void> update() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (this.prepareUpdateBody(body)) {
            return new RestRequest(this.channel.getApi(), RestMethod.PATCH, RestEndpoint.CHANNEL).setUrlParameters(this.channel.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchChannel = false;
        if (this.name != null) {
            body.put("name", this.name);
            patchChannel = true;
        }
        return patchChannel;
    }
}

