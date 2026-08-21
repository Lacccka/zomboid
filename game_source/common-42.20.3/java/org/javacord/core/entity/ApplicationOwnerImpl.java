/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.ApplicationOwner;
import org.javacord.api.entity.user.User;

public class ApplicationOwnerImpl
implements ApplicationOwner {
    private final DiscordApi api;
    private final long id;
    private final String name;
    private final String discriminator;

    public ApplicationOwnerImpl(DiscordApi api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.name = data.get("username").asText();
        this.discriminator = data.get("discriminator").asText();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDiscriminator() {
        return this.discriminator;
    }

    @Override
    public CompletableFuture<User> requestUser() {
        return this.api.getUserById(this.id);
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }
}

