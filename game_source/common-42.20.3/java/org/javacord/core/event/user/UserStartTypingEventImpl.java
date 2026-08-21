/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import java.util.Optional;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.EventImpl;

public class UserStartTypingEventImpl
extends EventImpl
implements UserStartTypingEvent {
    private final TextChannel channel;
    private final long userId;
    private final Member member;

    public UserStartTypingEventImpl(TextChannel channel, long userId, Member member) {
        super(channel.getApi());
        this.channel = channel;
        this.userId = userId;
        this.member = member;
    }

    @Override
    public TextChannel getChannel() {
        return this.channel;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public Optional<User> getUser() {
        return Optional.ofNullable(this.member).map(Member::getUser);
    }
}

