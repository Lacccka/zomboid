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
import org.javacord.api.interaction.internal.ApplicationCommandUpdaterDelegate;

public abstract class ApplicationCommandUpdater<T extends ApplicationCommand, D extends ApplicationCommandUpdaterDelegate<T>, B extends ApplicationCommandUpdater<T, D, B>> {
    private final D delegate;

    protected ApplicationCommandUpdater(D delegate) {
        this.delegate = delegate;
    }

    public B setName(String name) {
        this.delegate.setName(name);
        return (B)this;
    }

    public B addNameLocalization(DiscordLocale locale, String localization) {
        this.delegate.addNameLocalization(locale, localization);
        return (B)this;
    }

    public B setDescription(String description) {
        this.delegate.setDescription(description);
        return (B)this;
    }

    public B addDescriptionLocalization(DiscordLocale locale, String localization) {
        this.delegate.addDescriptionLocalization(locale, localization);
        return (B)this;
    }

    public B setDefaultEnabledForPermissions(PermissionType ... requiredPermissions) {
        this.delegate.setDefaultEnabledForPermissions(EnumSet.copyOf(Arrays.asList(requiredPermissions)));
        return (B)this;
    }

    public B setDefaultEnabledForPermissions(EnumSet<PermissionType> requiredPermissions) {
        this.delegate.setDefaultEnabledForPermissions(requiredPermissions);
        return (B)this;
    }

    public B setDefaultEnabledForEveryone() {
        this.delegate.setDefaultEnabledForEveryone();
        return (B)this;
    }

    public B setDefaultDisabled() {
        this.delegate.setDefaultDisabled();
        return (B)this;
    }

    public B setEnabledInDms(boolean enabledInDms) {
        this.delegate.setEnabledInDms(enabledInDms);
        return (B)this;
    }

    public CompletableFuture<T> updateGlobal(DiscordApi api) {
        return this.delegate.updateGlobal(api);
    }

    public CompletableFuture<T> updateForServer(Server server) {
        return this.delegate.updateForServer(server.getApi(), server.getId());
    }

    public CompletableFuture<T> updateForServer(DiscordApi api, long server) {
        return this.delegate.updateForServer(api, server);
    }

    public D getDelegate() {
        return this.delegate;
    }
}

