/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import java.util.concurrent.CompletableFuture;

public interface ServerChannelUpdaterDelegate {
    public void setAuditLogReason(String var1);

    public void setName(String var1);

    public CompletableFuture<Void> update();
}

