/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeUserLimitEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public class ServerVoiceChannelChangeUserLimitEventImpl
extends ServerVoiceChannelEventImpl
implements ServerVoiceChannelChangeUserLimitEvent {
    private final int newUserLimit;
    private final int oldUserLimit;

    public ServerVoiceChannelChangeUserLimitEventImpl(ServerVoiceChannel channel, int newUserLimit, int oldUserLimit) {
        super(channel);
        this.newUserLimit = newUserLimit;
        this.oldUserLimit = oldUserLimit;
    }

    @Override
    public Optional<Integer> getNewUserLimit() {
        return this.newUserLimit == 0 ? Optional.empty() : Optional.of(this.newUserLimit);
    }

    @Override
    public Optional<Integer> getOldUserLimit() {
        return this.oldUserLimit == 0 ? Optional.empty() : Optional.of(this.oldUserLimit);
    }
}

