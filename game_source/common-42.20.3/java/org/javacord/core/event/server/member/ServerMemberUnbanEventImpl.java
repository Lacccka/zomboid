/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.member;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.core.event.server.member.ServerMemberEventImpl;

public class ServerMemberUnbanEventImpl
extends ServerMemberEventImpl
implements ServerMemberUnbanEvent {
    public ServerMemberUnbanEventImpl(Server server, User user) {
        super(server, user);
    }
}

