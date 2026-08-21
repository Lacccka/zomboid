/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

import java.util.Optional;

public interface WelcomeScreenChannel {
    public long getChannelId();

    public String getDescription();

    public Optional<Long> getEmojiId();

    public Optional<String> getEmojiName();
}

