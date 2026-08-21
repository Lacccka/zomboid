/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.member;

import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ServerMemberUnbanListener
extends ServerAttachableListener,
UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerMemberUnban(ServerMemberUnbanEvent var1);
}

