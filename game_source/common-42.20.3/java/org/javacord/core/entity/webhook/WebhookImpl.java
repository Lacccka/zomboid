/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.entity.webhook.WebhookType;
import org.javacord.api.util.Specializable;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.webhook.IncomingWebhookImpl;
import org.javacord.core.listener.webhook.InternalWebhookAttachableListenerManager;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class WebhookImpl
implements Webhook,
Specializable<WebhookImpl>,
InternalWebhookAttachableListenerManager {
    private static final Logger logger = LoggerUtil.getLogger(WebhookImpl.class);
    private final DiscordApiImpl api;
    private final long id;
    private final Long serverId;
    private final long channelId;
    private final User user;
    private final String name;
    private final String avatarId;
    private final WebhookType type;

    protected WebhookImpl(DiscordApi api, JsonNode data) {
        this.api = (DiscordApiImpl)api;
        this.type = WebhookType.fromValue(data.get("type").asInt());
        this.id = Long.parseLong(data.get("id").asText());
        this.serverId = data.has("guild_id") ? Long.valueOf(Long.parseLong(data.get("guild_id").asText())) : null;
        this.channelId = Long.parseLong(data.get("channel_id").asText());
        this.user = data.has("user") ? new UserImpl((DiscordApiImpl)api, data.get("user"), (MemberImpl)null, null) : null;
        this.name = data.has("name") && !data.get("name").isNull() ? data.get("name").asText() : null;
        this.avatarId = data.has("avatar") && !data.get("avatar").isNull() ? data.get("avatar").asText() : null;
    }

    public static WebhookImpl createWebhook(DiscordApi api, JsonNode data) {
        if (data.hasNonNull("token")) {
            return new IncomingWebhookImpl(api, data);
        }
        return new WebhookImpl(api, data);
    }

    public static List<Webhook> createAllIncomingWebhooksFromJsonArray(DiscordApi api, JsonNode jsonArray) {
        ArrayList<WebhookImpl> webhooks = new ArrayList<WebhookImpl>();
        for (JsonNode webhookJson : jsonArray) {
            if (WebhookType.fromValue(webhookJson.get("type").asInt()) != WebhookType.INCOMING) continue;
            webhooks.add(WebhookImpl.createWebhook(api, webhookJson));
        }
        return Collections.unmodifiableList(webhooks);
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
    public Optional<Long> getServerId() {
        return Optional.ofNullable(this.serverId);
    }

    @Override
    public Optional<Server> getServer() {
        return this.getServerId().flatMap(this.api::getServerById);
    }

    @Override
    public Optional<TextChannel> getChannel() {
        return this.api.getTextChannelById(this.getChannelId());
    }

    @Override
    public long getChannelId() {
        return this.channelId;
    }

    @Override
    public WebhookType getType() {
        return this.type;
    }

    @Override
    public Optional<IncomingWebhook> asIncomingWebhook() {
        return Optional.empty();
    }

    @Override
    public Optional<User> getCreator() {
        return Optional.ofNullable(this.user);
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    @Override
    public Optional<Icon> getAvatar() {
        if (this.avatarId != null) {
            String url = "https://cdn.discordapp.com/avatars/" + this.getIdAsString() + "/" + this.avatarId + (this.avatarId.startsWith("a_") ? ".gif" : ".png");
            try {
                return Optional.of(new IconImpl(this.getApi(), new URL(url)));
            }
            catch (MalformedURLException e) {
                logger.warn("Seems like the url of the avatar is malformed! Please contact the developer!", (Throwable)e);
            }
        }
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.WEBHOOK).setUrlParameters(this.getIdAsString()).setAuditLogReason(reason).execute(result -> null);
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("Webhook (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

