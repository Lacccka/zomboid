/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public abstract class ApplicationCommandImpl
implements ApplicationCommand {
    private final DiscordApiImpl api;
    private final long id;
    private final long applicationId;
    private final String name;
    private final Map<DiscordLocale, String> nameLocalizations = new HashMap<DiscordLocale, String>();
    private final String description;
    private final Map<DiscordLocale, String> descriptionLocalizations = new HashMap<DiscordLocale, String>();
    private final EnumSet<PermissionType> defaultMemberPermission;
    private final boolean dmPermission;
    private final boolean nsfw;
    private final Server server;
    private final Long serverId;

    protected ApplicationCommandImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.applicationId = data.get("application_id").asLong();
        this.name = data.get("name").asText();
        data.path("name_localizations").fields().forEachRemaining(e -> this.nameLocalizations.put(DiscordLocale.fromLocaleCode((String)e.getKey()), ((JsonNode)e.getValue()).asText()));
        this.description = data.get("description").asText();
        Long defaultMemberPermissionsBitset = data.hasNonNull("default_member_permissions") ? Long.valueOf(data.get("default_member_permissions").asLong()) : null;
        this.defaultMemberPermission = defaultMemberPermissionsBitset != null ? Arrays.stream(PermissionType.values()).filter(type -> type.isSet(defaultMemberPermissionsBitset)).collect(Collectors.toCollection(() -> EnumSet.noneOf(PermissionType.class))) : null;
        data.path("description_localizations").fields().forEachRemaining(e -> this.descriptionLocalizations.put(DiscordLocale.fromLocaleCode((String)e.getKey()), ((JsonNode)e.getValue()).asText()));
        this.serverId = data.has("guild_id") ? Long.valueOf(data.get("guild_id").asLong()) : null;
        this.server = this.serverId != null ? (Server)api.getPossiblyUnreadyServerById(this.serverId).orElse(null) : null;
        this.dmPermission = this.server == null && (!data.has("dm_permission") || data.get("dm_permission").asBoolean());
        this.nsfw = data.has("nsfw") && data.get("nsfw").asBoolean();
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public long getApplicationId() {
        return this.applicationId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<DiscordLocale, String> getNameLocalizations() {
        return Collections.unmodifiableMap(this.nameLocalizations);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Map<DiscordLocale, String> getDescriptionLocalizations() {
        return Collections.unmodifiableMap(this.descriptionLocalizations);
    }

    @Override
    public Optional<EnumSet<PermissionType>> getDefaultRequiredPermissions() {
        return this.defaultMemberPermission != null ? Optional.of(EnumSet.copyOf(this.defaultMemberPermission)) : Optional.empty();
    }

    @Override
    public boolean isDisabledByDefault() {
        return this.defaultMemberPermission.isEmpty();
    }

    @Override
    public boolean isEnabledInDms() {
        return this.server != null && this.dmPermission;
    }

    @Override
    public Optional<Long> getServerId() {
        return Optional.ofNullable(this.serverId);
    }

    @Override
    public Optional<Server> getServer() {
        return Optional.ofNullable(this.server);
    }

    @Override
    public boolean isGlobalApplicationCommand() {
        return this.serverId == null;
    }

    @Override
    public boolean isServerApplicationCommand() {
        return this.serverId != null;
    }

    @Override
    public boolean isNsfw() {
        return this.nsfw;
    }

    @Override
    public CompletableFuture<Void> delete() {
        return (this.isGlobalApplicationCommand() ? new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getApplicationId()), this.getIdAsString()) : new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getApplicationId()), Long.toUnsignedString(this.serverId), this.getIdAsString())).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteGlobal() {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getApplicationId()), this.getIdAsString()).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteForServer(long server) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(this.getApplicationId()), String.valueOf(server), this.getIdAsString()).execute(result -> null);
    }
}

