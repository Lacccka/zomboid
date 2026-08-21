/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.member;

import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ServerMemberBanListener
extends ServerAttachableListener,
UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerMemberBan(ServerMemberBanEvent var1);
}

