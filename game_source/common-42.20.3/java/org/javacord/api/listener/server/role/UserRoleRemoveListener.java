/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.role;

import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.role.RoleAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface UserRoleRemoveListener
extends ServerAttachableListener,
UserAttachableListener,
RoleAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onUserRoleRemove(UserRoleRemoveEvent var1);
}

