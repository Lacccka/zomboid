/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import org.javacord.api.event.channel.server.ServerChannelChangeNsfwFlagEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerChannelChangeNsfwFlagListener
extends ServerAttachableListener,
ServerTextChannelAttachableListener,
ChannelCategoryAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChannelChangeNsfwFlag(ServerChannelChangeNsfwFlagEvent var1);
}

