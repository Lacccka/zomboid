/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.role;

import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface RoleCreateListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onRoleCreate(RoleCreateEvent var1);
}

