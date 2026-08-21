/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.WebhookType;
import org.javacord.core.entity.webhook.WebhookImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class IncomingWebhookImpl
extends WebhookImpl
implements IncomingWebhook {
    private final String token;

    public IncomingWebhookImpl(DiscordApi api, JsonNode data) {
        super(api, data);
        this.token = data.get("token").asText();
    }

    public static List<IncomingWebhook> createIncomingWebhooksFromJsonArray(DiscordApi api, JsonNode jsonArray) {
        ArrayList<IncomingWebhookImpl> webhooks = new ArrayList<IncomingWebhookImpl>();
        for (JsonNode webhookJson : jsonArray) {
            if (WebhookType.fromValue(webhookJson.get("type").asInt()) != WebhookType.INCOMING || !webhookJson.hasNonNull("token")) continue;
            webhooks.add(new IncomingWebhookImpl(api, webhookJson));
        }
        return Collections.unmodifiableList(webhooks);
    }

    @Override
    public Optional<IncomingWebhook> asIncomingWebhook() {
        return Optional.of(this);
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.WEBHOOK).setUrlParameters(this.getIdAsString(), this.getToken()).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public String getToken() {
        return this.token;
    }
}

