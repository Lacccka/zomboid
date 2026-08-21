/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.auditlog;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

public interface AuditLogEntryTarget
extends DiscordEntity {
    public AuditLogEntry getAuditLogEntry();

    default public CompletableFuture<User> asUser() {
        return this.getApi().getUserById(this.getId());
    }

    default public Optional<Server> asServer() {
        return this.getApi().getServerById(this.getId());
    }

    default public Optional<ServerChannel> asChannel() {
        return this.getApi().getServerChannelById(this.getId());
    }

    default public Optional<Role> asRole() {
        return this.getApi().getRoleById(this.getId());
    }

    default public Optional<Webhook> asWebhook() {
        for (Webhook webhook : this.getAuditLogEntry().getAuditLog().getInvolvedWebhooks()) {
            if (webhook.getId() != this.getId()) continue;
            return Optional.of(webhook);
        }
        return Optional.empty();
    }
}

