/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.text;

import org.javacord.api.event.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerTextChannelChangeDefaultAutoArchiveDurationListener
extends ServerAttachableListener,
ServerTextChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerTextChannelChangeDefaultAutoArchiveDuration(ServerTextChannelChangeDefaultAutoArchiveDurationEvent var1);
}

