/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.user;

import org.javacord.api.event.user.UserChangeActivityEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface UserChangeActivityListener
extends ServerAttachableListener,
UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onUserChangeActivity(UserChangeActivityEvent var1);
}

