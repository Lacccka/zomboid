/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public interface InteractionBase
extends DiscordEntity {
    public long getApplicationId();

    public InteractionType getType();

    public InteractionImmediateResponseBuilder createImmediateResponder();

    public CompletableFuture<InteractionOriginalResponseUpdater> respondLater();

    public CompletableFuture<InteractionOriginalResponseUpdater> respondLater(boolean var1);

    default public CompletableFuture<Void> respondWithModal(String customId, String title, HighLevelComponent ... components) {
        return this.respondWithModal(customId, title, Arrays.asList(components));
    }

    public CompletableFuture<Void> respondWithModal(String var1, String var2, List<HighLevelComponent> var3);

    public InteractionFollowupMessageBuilder createFollowupMessageBuilder();

    public Optional<Server> getServer();

    public Optional<TextChannel> getChannel();

    public User getUser();

    public String getToken();

    public int getVersion();

    public DiscordLocale getLocale();

    public Optional<DiscordLocale> getServerLocale();

    public Optional<EnumSet<PermissionType>> getBotPermissions();
}

