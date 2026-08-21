/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.voice;

import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ServerVoiceChannelMemberLeaveListener
extends ServerAttachableListener,
UserAttachableListener,
ServerVoiceChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent var1);
}

