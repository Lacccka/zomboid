/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.server.invite.WelcomeScreenChannel;

public interface WelcomeScreen {
    public Optional<String> getDescription();

    public List<WelcomeScreenChannel> getWelcomeScreenChannels();
}

