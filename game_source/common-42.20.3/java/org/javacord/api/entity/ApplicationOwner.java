/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.user.User;

public interface ApplicationOwner
extends DiscordEntity,
Nameable {
    @Override
    public String getName();

    public String getDiscriminator();

    default public String getDiscriminatedName() {
        return this.getName() + "#" + this.getDiscriminator();
    }

    public CompletableFuture<User> requestUser();
}

