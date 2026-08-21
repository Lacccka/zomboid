/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.VoiceStateUpdateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;

@FunctionalInterface
public interface VoiceStateUpdateListener
extends ServerChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onVoiceStateUpdate(VoiceStateUpdateEvent var1);
}

