/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message;

import org.javacord.api.event.message.ChannelPinsUpdateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ChannelPinsUpdateListener
extends TextChannelAttachableListener,
ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onChannelPinsUpdate(ChannelPinsUpdateEvent var1);
}

