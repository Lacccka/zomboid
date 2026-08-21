/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerChannelUpdater<T extends ServerChannelUpdater<T>> {
    private final ServerChannelUpdaterDelegate serverChannelUpdaterDelegate;

    protected ServerChannelUpdater(ServerChannelUpdaterDelegate serverChannelUpdaterDelegate) {
        this.serverChannelUpdaterDelegate = serverChannelUpdaterDelegate;
    }

    public ServerChannelUpdater(ServerChannel serverChannel) {
        this.serverChannelUpdaterDelegate = DelegateFactory.createServerChannelUpdaterDelegate(serverChannel);
    }

    public T setAuditLogReason(String reason) {
        this.serverChannelUpdaterDelegate.setAuditLogReason(reason);
        return (T)this;
    }

    public T setName(String name) {
        this.serverChannelUpdaterDelegate.setName(name);
        return (T)this;
    }

    public CompletableFuture<Void> update() {
        return this.serverChannelUpdaterDelegate.update();
    }
}

