/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.InteractionCallbackType;
import org.javacord.core.entity.message.component.ComponentImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.interaction.InteractionFollowupMessageBuilderImpl;
import org.javacord.core.interaction.InteractionImmediateResponseBuilderImpl;
import org.javacord.core.interaction.InteractionOriginalResponseUpdaterImpl;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public abstract class InteractionImpl
implements Interaction {
    private static final Logger logger = LoggerUtil.getLogger(InteractionImpl.class);
    private static final String RESPOND_LATER_BODY = "{\"type\": " + InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getId() + "}";
    private static final String RESPOND_LATER_EPHEMERAL_BODY = "{\"type\": " + InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getId() + ", \"data\": {\"flags\": " + MessageFlag.EPHEMERAL.getId() + "}}";
    private final DiscordApiImpl api;
    private final TextChannel channel;
    private final long id;
    private final long applicationId;
    private final UserImpl user;
    private final String token;
    private final int version;
    private final DiscordLocale locale;
    private final DiscordLocale serverLocale;
    private final EnumSet<PermissionType> appPermissions;

    public InteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        this.api = api;
        this.channel = channel;
        this.id = jsonData.get("id").asLong();
        this.applicationId = jsonData.get("application_id").asLong();
        if (jsonData.hasNonNull("member")) {
            MemberImpl member = new MemberImpl(api, (ServerImpl)this.getServer().orElseThrow(AssertionError::new), jsonData.get("member"), null);
            this.user = (UserImpl)member.getUser();
        } else if (jsonData.hasNonNull("user")) {
            this.user = new UserImpl(api, jsonData.get("user"), (MemberImpl)null, null);
        } else {
            this.user = null;
            logger.error("Received interaction without a member AND without a user field");
        }
        this.token = jsonData.get("token").asText();
        this.version = jsonData.get("version").asInt();
        this.locale = DiscordLocale.fromLocaleCode(jsonData.get("locale").asText());
        this.serverLocale = jsonData.hasNonNull("guild_locale") ? DiscordLocale.fromLocaleCode(jsonData.get("guild_locale").asText()) : null;
        Long appPermissionsBitset = jsonData.hasNonNull("app_permissions") ? Long.valueOf(jsonData.get("app_permissions").asLong()) : null;
        this.appPermissions = appPermissionsBitset != null ? Arrays.stream(PermissionType.values()).filter(type -> type.isSet(appPermissionsBitset)).collect(Collectors.toCollection(() -> EnumSet.noneOf(PermissionType.class))) : null;
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
    public abstract InteractionType getType();

    @Override
    public InteractionImmediateResponseBuilder createImmediateResponder() {
        return new InteractionImmediateResponseBuilderImpl(this);
    }

    @Override
    public CompletableFuture<InteractionOriginalResponseUpdater> respondLater() {
        return this.respondLater(false);
    }

    @Override
    public CompletableFuture<InteractionOriginalResponseUpdater> respondLater(boolean ephemeral) {
        return new RestRequest(this.api, RestMethod.POST, RestEndpoint.INTERACTION_RESPONSE).setUrlParameters(this.getIdAsString(), this.token).consumeGlobalRatelimit(false).includeAuthorizationHeader(false).setBody(ephemeral ? RESPOND_LATER_EPHEMERAL_BODY : RESPOND_LATER_BODY).execute(result -> new InteractionOriginalResponseUpdaterImpl(this));
    }

    @Override
    public CompletableFuture<Void> respondWithModal(String customId, String title, List<HighLevelComponent> components) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", InteractionCallbackType.MODAL.getId());
        ObjectNode modal = JsonNodeFactory.instance.objectNode();
        modal.put("custom_id", customId);
        modal.put("title", title);
        ArrayNode comps = JsonNodeFactory.instance.arrayNode();
        components.forEach(highLevelComponent -> comps.add(((ComponentImpl)((Object)highLevelComponent)).toJsonNode()));
        modal.set("components", comps);
        body.set("data", modal);
        return new RestRequest(this.api, RestMethod.POST, RestEndpoint.INTERACTION_RESPONSE).setUrlParameters(this.getIdAsString(), this.token).includeAuthorizationHeader(false).consumeGlobalRatelimit(false).setBody(body).execute(result -> null);
    }

    @Override
    public InteractionFollowupMessageBuilder createFollowupMessageBuilder() {
        return new InteractionFollowupMessageBuilderImpl(this);
    }

    @Override
    public Optional<Server> getServer() {
        return this.getChannel().flatMap(Channel::asServerChannel).map(ServerChannel::getServer);
    }

    @Override
    public Optional<TextChannel> getChannel() {
        return Optional.ofNullable(this.channel);
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public DiscordLocale getLocale() {
        return this.locale;
    }

    @Override
    public Optional<DiscordLocale> getServerLocale() {
        return Optional.ofNullable(this.serverLocale);
    }

    @Override
    public Optional<EnumSet<PermissionType>> getBotPermissions() {
        return this.appPermissions != null ? Optional.of(EnumSet.copyOf(this.appPermissions)) : Optional.empty();
    }
}

