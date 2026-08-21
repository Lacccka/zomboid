/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.user;

import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface UserChangeNameListener
extends ServerAttachableListener,
UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onUserChangeName(UserChangeNameEvent var1);
}

