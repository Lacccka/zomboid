/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.interaction;

import org.javacord.api.event.interaction.UserContextMenuCommandEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

public interface UserContextMenuCommandListener
extends ServerAttachableListener,
UserAttachableListener,
TextChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onUserContextMenuCommand(UserContextMenuCommandEvent var1);
}

