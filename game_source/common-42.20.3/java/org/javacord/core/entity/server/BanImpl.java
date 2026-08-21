/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.server.Ban;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;

public class BanImpl
implements Ban {
    private final Server server;
    private final User user;
    private final String reason;

    public BanImpl(Server server, JsonNode data) {
        this.server = server;
        this.user = new UserImpl((DiscordApiImpl)server.getApi(), data.get("user"), (MemberImpl)null, (ServerImpl)server);
        this.reason = data.has("reason") ? data.get("reason").asText() : null;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason);
    }
}

