/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server.invite;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.server.invite.WelcomeScreen;
import org.javacord.api.entity.server.invite.WelcomeScreenChannel;
import org.javacord.core.entity.server.invite.WelcomeScreenChannelImpl;

public class WelcomeScreenImpl
implements WelcomeScreen {
    private final String description;
    private final List<WelcomeScreenChannel> welcomeScreenChannels = new ArrayList<WelcomeScreenChannel>();

    public WelcomeScreenImpl(JsonNode data) {
        this.description = data.get("description").asText();
        for (JsonNode welcomeChannel : data.get("welcome_channels")) {
            this.welcomeScreenChannels.add(new WelcomeScreenChannelImpl(welcomeChannel));
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public List<WelcomeScreenChannel> getWelcomeScreenChannels() {
        return Collections.unmodifiableList(this.welcomeScreenChannels);
    }
}

