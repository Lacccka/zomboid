/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.internal.ApplicationCommandBuilderDelegate;

public abstract class ApplicationCommandBuilder<R extends ApplicationCommand, D extends ApplicationCommandBuilderDelegate<R>, T extends ApplicationCommandBuilder<R, D, T>> {
    private final D delegate;

    protected ApplicationCommandBuilder(D delegate) {
        this.delegate = delegate;
    }

    public T setName(String name) {
        this.delegate.setName(name);
        return (T)this;
    }

    public T addNameLocalization(DiscordLocale locale, String localization) {
        this.delegate.addNameLocalization(locale, localization);
        return (T)this;
    }

    public T setDescription(String description) {
        this.delegate.setDescription(description);
        return (T)this;
    }

    public T addDescriptionLocalization(DiscordLocale locale, String localization) {
        this.delegate.addDescriptionLocalization(locale, localization);
        return (T)this;
    }

    public T setDefaultEnabledForPermissions(PermissionType ... requiredPermissions) {
        this.delegate.setDefaultEnabledForPermissions(EnumSet.copyOf(Arrays.asList(requiredPermissions)));
        return (T)this;
    }

    public T setDefaultEnabledForPermissions(EnumSet<PermissionType> requiredPermissions) {
        this.delegate.setDefaultEnabledForPermissions(requiredPermissions);
        return (T)this;
    }

    public T setDefaultEnabledForEveryone() {
        this.delegate.setDefaultEnabledForEveryone();
        return (T)this;
    }

    public T setDefaultDisabled() {
        this.delegate.setDefaultDisabled();
        return (T)this;
    }

    public T setEnabledInDms(boolean enabledInDms) {
        this.delegate.setEnabledInDms(enabledInDms);
        return (T)this;
    }

    public T setNsfw(boolean nsfw) {
        this.delegate.setNsfw(nsfw);
        return (T)this;
    }

    public D getDelegate() {
        return this.delegate;
    }

    public CompletableFuture<R> createGlobal(DiscordApi api) {
        return this.delegate.createGlobal(api);
    }

    public CompletableFuture<R> createForServer(Server server) {
        return this.delegate.createForServer(server.getApi(), server.getId());
    }

    public CompletableFuture<R> createForServer(DiscordApi api, long server) {
        return this.delegate.createForServer(api, server);
    }
}

