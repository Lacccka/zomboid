/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import org.javacord.api.event.channel.server.ServerChannelChangeOverwrittenPermissionsEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.role.RoleAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ServerChannelChangeOverwrittenPermissionsListener
extends ServerAttachableListener,
UserAttachableListener,
ServerChannelAttachableListener,
RoleAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChannelChangeOverwrittenPermissions(ServerChannelChangeOverwrittenPermissionsEvent var1);
}

