/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;
import org.javacord.api.event.user.UserEvent;

public interface ServerVoiceChannelMemberLeaveEvent
extends ServerVoiceChannelEvent,
UserEvent {
    public Optional<ServerVoiceChannel> getNewChannel();

    public boolean isMove();
}

