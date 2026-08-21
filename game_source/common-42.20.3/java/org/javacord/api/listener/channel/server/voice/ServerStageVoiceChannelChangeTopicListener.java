/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.voice;

import org.javacord.api.event.channel.server.voice.ServerStageVoiceChannelChangeTopicEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerStageVoiceChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

public interface ServerStageVoiceChannelChangeTopicListener
extends ServerAttachableListener,
ServerStageVoiceChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerStageVoiceChannelChangeTopic(ServerStageVoiceChannelChangeTopicEvent var1);
}

