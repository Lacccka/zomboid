/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberEventImpl;

public class ServerVoiceChannelMemberJoinEventImpl
extends ServerVoiceChannelMemberEventImpl
implements ServerVoiceChannelMemberJoinEvent {
    private final ServerVoiceChannel oldChannel;

    public ServerVoiceChannelMemberJoinEventImpl(Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel) {
        super(member, newChannel);
        this.oldChannel = oldChannel;
    }

    @Override
    public Optional<ServerVoiceChannel> getOldChannel() {
        return Optional.ofNullable(this.oldChannel);
    }

    @Override
    public boolean isMove() {
        return this.oldChannel != null;
    }
}

