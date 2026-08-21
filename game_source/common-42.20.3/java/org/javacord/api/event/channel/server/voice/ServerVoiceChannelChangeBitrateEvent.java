/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;

public interface ServerVoiceChannelChangeBitrateEvent
extends ServerVoiceChannelEvent {
    public int getNewBitrate();

    public int getOldBitrate();
}

