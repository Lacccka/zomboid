/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberEventImpl;

public class ServerVoiceChannelMemberLeaveEventImpl
extends ServerVoiceChannelMemberEventImpl
implements ServerVoiceChannelMemberLeaveEvent {
    private final ServerVoiceChannel newChannel;

    public ServerVoiceChannelMemberLeaveEventImpl(Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel) {
        super(member, oldChannel);
        this.newChannel = newChannel;
    }

    @Override
    public Optional<ServerVoiceChannel> getNewChannel() {
        return Optional.ofNullable(this.newChannel);
    }

    @Override
    public boolean isMove() {
        return this.newChannel != null;
    }
}

