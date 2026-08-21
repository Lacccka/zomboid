/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.util.Specializable;

public interface ApplicationCommand
extends DiscordEntity,
Specializable<ApplicationCommand> {
    @Override
    public long getId();

    public long getApplicationId();

    public ApplicationCommandType getType();

    public String getName();

    public Map<DiscordLocale, String> getNameLocalizations();

    public String getDescription();

    public Map<DiscordLocale, String> getDescriptionLocalizations();

    public Optional<EnumSet<PermissionType>> getDefaultRequiredPermissions();

    public boolean isDisabledByDefault();

    public boolean isEnabledInDms();

    public Optional<Long> getServerId();

    public Optional<Server> getServer();

    public boolean isGlobalApplicationCommand();

    public boolean isServerApplicationCommand();

    public boolean isNsfw();

    public CompletableFuture<Void> delete();

    @Deprecated
    public CompletableFuture<Void> deleteGlobal();

    @Deprecated
    default public CompletableFuture<Void> deleteForServer(Server server) {
        return this.deleteForServer(server.getId());
    }

    @Deprecated
    public CompletableFuture<Void> deleteForServer(long var1);
}

