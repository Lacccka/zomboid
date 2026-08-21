/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.auditlog;

import java.util.List;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

public interface AuditLog {
    public DiscordApi getApi();

    public Server getServer();

    public Set<Webhook> getInvolvedWebhooks();

    public Set<User> getInvolvedUsers();

    public List<AuditLogEntry> getEntries();
}

