/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message;

import org.javacord.api.event.message.CachedMessagePinEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface CachedMessagePinListener
extends ServerAttachableListener,
TextChannelAttachableListener,
MessageAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onCachedMessagePin(CachedMessagePinEvent var1);
}

