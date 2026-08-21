/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.internal;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.DiscordLocale;

public interface ApplicationCommandBuilderDelegate<T extends ApplicationCommand> {
    public void setName(String var1);

    public void addNameLocalization(DiscordLocale var1, String var2);

    public void setDescription(String var1);

    public void addDescriptionLocalization(DiscordLocale var1, String var2);

    public void setDefaultEnabledForPermissions(EnumSet<PermissionType> var1);

    public void setDefaultEnabledForEveryone();

    public void setDefaultDisabled();

    public void setEnabledInDms(boolean var1);

    public void setNsfw(boolean var1);

    public CompletableFuture<T> createGlobal(DiscordApi var1);

    public CompletableFuture<T> createForServer(DiscordApi var1, long var2);
}

