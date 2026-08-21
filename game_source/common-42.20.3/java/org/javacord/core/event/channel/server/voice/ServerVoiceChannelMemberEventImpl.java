/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public abstract class ServerVoiceChannelMemberEventImpl
extends ServerVoiceChannelEventImpl {
    private final Member member;

    public ServerVoiceChannelMemberEventImpl(Member member, ServerVoiceChannel channel) {
        super(channel);
        this.member = member;
    }

    public User getUser() {
        return this.member.getUser();
    }
}

