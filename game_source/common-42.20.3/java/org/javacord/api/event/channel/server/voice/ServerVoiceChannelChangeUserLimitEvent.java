/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import java.util.Optional;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;

public interface ServerVoiceChannelChangeUserLimitEvent
extends ServerVoiceChannelEvent {
    public Optional<Integer> getNewUserLimit();

    public Optional<Integer> getOldUserLimit();
}

