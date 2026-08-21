/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.internal.ServerChannelBuilderDelegate;
import org.javacord.core.entity.server.ServerImpl;

public class ServerChannelBuilderDelegateImpl
implements ServerChannelBuilderDelegate {
    protected final ServerImpl server;
    protected String reason = null;
    private String name = null;

    protected ServerChannelBuilderDelegateImpl(ServerImpl server) {
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

    protected void prepareBody(ObjectNode body) {
        if (this.name == null) {
            throw new IllegalStateException("Name is no optional parameter!");
        }
        body.put("name", this.name);
    }
}

