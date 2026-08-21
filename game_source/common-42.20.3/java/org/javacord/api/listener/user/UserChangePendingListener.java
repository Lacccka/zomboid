/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.user;

import org.javacord.api.event.user.UserChangePendingEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

public interface UserChangePendingListener
extends ServerAttachableListener,
UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerMemberChangePending(UserChangePendingEvent var1);
}

