/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.member;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.core.event.server.member.ServerMemberEventImpl;

public class ServerMemberJoinEventImpl
extends ServerMemberEventImpl
implements ServerMemberJoinEvent {
    public ServerMemberJoinEventImpl(Server server, User user) {
        super(server, user);
    }
}

