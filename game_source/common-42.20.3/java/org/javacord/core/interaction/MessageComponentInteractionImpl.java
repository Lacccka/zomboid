/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.InteractionCallbackType;
import org.javacord.core.interaction.ComponentInteractionOriginalMessageUpdaterImpl;
import org.javacord.core.interaction.InteractionImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public abstract class MessageComponentInteractionImpl
extends InteractionImpl
implements MessageComponentInteraction {
    private static final String UPDATE_LATER_BODY = "{\"type\": " + InteractionCallbackType.DEFERRED_UPDATE_MESSAGE.getId() + "}";
    private final Message message;
    private final String customId;

    public MessageComponentInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        this.message = api.getOrCreateMessage(channel, jsonData.get("message"));
        JsonNode data = jsonData.get("data");
        this.customId = data.get("custom_id").asText();
    }

    @Override
    public InteractionType getType() {
        return InteractionType.MESSAGE_COMPONENT;
    }

    @Override
    public CompletableFuture<Void> acknowledge() {
        return new RestRequest(this.getApi(), RestMethod.POST, RestEndpoint.INTERACTION_RESPONSE).setUrlParameters(this.getIdAsString(), this.getToken()).consumeGlobalRatelimit(false).includeAuthorizationHeader(false).setBody(UPDATE_LATER_BODY).execute(result -> null);
    }

    @Override
    public ComponentInteractionOriginalMessageUpdaterImpl createOriginalMessageUpdater() {
        return new ComponentInteractionOriginalMessageUpdaterImpl(this);
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public String getCustomId() {
        return this.customId;
    }

    @Override
    public abstract ComponentType getComponentType();
}

