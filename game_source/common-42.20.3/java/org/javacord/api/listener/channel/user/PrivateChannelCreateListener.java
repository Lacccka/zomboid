/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.user;

import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface PrivateChannelCreateListener
extends UserAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onPrivateChannelCreate(PrivateChannelCreateEvent var1);
}

