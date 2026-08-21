/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.auditlog.AuditLogEntryImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.webhook.WebhookImpl;

public class AuditLogImpl
implements AuditLog {
    private final DiscordApi api;
    private final Server server;
    private final Set<Webhook> involvedWebhooks = new HashSet<Webhook>();
    private final Set<User> involvedUsers = new HashSet<User>();
    private final List<AuditLogEntry> entries = new ArrayList<AuditLogEntry>();

    public AuditLogImpl(Server server) {
        this.api = server.getApi();
        this.server = server;
    }

    public void addEntries(JsonNode data) {
        boolean alreadyAdded;
        for (JsonNode webhookJson : data.get("webhooks")) {
            alreadyAdded = this.involvedWebhooks.stream().anyMatch(webhook -> webhook.getId() == webhookJson.get("id").asLong());
            if (alreadyAdded) continue;
            this.involvedWebhooks.add(WebhookImpl.createWebhook(this.api, webhookJson));
        }
        for (JsonNode userJson : data.get("users")) {
            alreadyAdded = this.involvedUsers.stream().anyMatch(user -> user.getId() == userJson.get("id").asLong());
            if (alreadyAdded) continue;
            this.involvedUsers.add(new UserImpl((DiscordApiImpl)this.api, userJson, (MemberImpl)null, (ServerImpl)this.server));
        }
        for (JsonNode entry : data.get("audit_log_entries")) {
            this.entries.add(new AuditLogEntryImpl(this, entry));
        }
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public Set<Webhook> getInvolvedWebhooks() {
        return Collections.unmodifiableSet(this.involvedWebhooks);
    }

    @Override
    public Set<User> getInvolvedUsers() {
        return Collections.unmodifiableSet(this.involvedUsers);
    }

    @Override
    public List<AuditLogEntry> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }
}

