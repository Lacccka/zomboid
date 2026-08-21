/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.event.server.member.ServerMemberEvent;

public interface UserChangePendingEvent
extends ServerMemberEvent {
    public boolean getOldPending();

    public boolean getNewPending();
}

