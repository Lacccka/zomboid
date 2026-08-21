/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.activity.ActivitySecrets;

public class ActivitySecretsImpl
implements ActivitySecrets {
    private final String join;
    private final String spectate;
    private final String match;

    public ActivitySecretsImpl(JsonNode data) {
        this.join = data.has("join") ? data.get("join").asText() : null;
        this.spectate = data.has("spectate") ? data.get("spectate").asText() : null;
        this.match = data.has("match") ? data.get("match").asText() : null;
    }

    @Override
    public Optional<String> getJoin() {
        return Optional.ofNullable(this.join);
    }

    @Override
    public Optional<String> getSpectate() {
        return Optional.ofNullable(this.spectate);
    }

    @Override
    public Optional<String> getMatch() {
        return Optional.ofNullable(this.match);
    }
}

