/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.role;

import org.javacord.api.event.server.role.RoleChangeNameEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.role.RoleAttachableListener;

@FunctionalInterface
public interface RoleChangeNameListener
extends ServerAttachableListener,
RoleAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onRoleChangeName(RoleChangeNameEvent var1);
}

