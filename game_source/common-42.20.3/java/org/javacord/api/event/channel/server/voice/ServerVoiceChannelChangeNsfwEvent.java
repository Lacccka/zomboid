/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;

public interface ServerVoiceChannelChangeNsfwEvent
extends ServerVoiceChannelEvent {
    public boolean isNsfw();

    public boolean wasNsfw();
}

