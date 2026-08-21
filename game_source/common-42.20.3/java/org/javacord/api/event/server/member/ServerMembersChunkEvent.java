/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.member;

import java.util.Set;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.ServerEvent;

public interface ServerMembersChunkEvent
extends ServerEvent {
    public Set<User> getMembers();
}

