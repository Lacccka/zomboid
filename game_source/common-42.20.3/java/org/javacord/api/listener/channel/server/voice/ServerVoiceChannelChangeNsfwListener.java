/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.voice;

import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeNsfwEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerVoiceChannelChangeNsfwListener
extends ServerAttachableListener,
ServerVoiceChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerVoiceChannelChangeNsfw(ServerVoiceChannelChangeNsfwEvent var1);
}

