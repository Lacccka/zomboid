/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.member;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.server.Ban;
import org.javacord.api.event.server.member.ServerMemberEvent;

public interface ServerMemberBanEvent
extends ServerMemberEvent {
    public CompletableFuture<Ban> requestBan();
}

