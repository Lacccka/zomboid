/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.auditlog;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogChange;
import org.javacord.api.entity.auditlog.AuditLogEntryTarget;
import org.javacord.api.entity.user.User;

public interface AuditLogEntry
extends DiscordEntity {
    public AuditLog getAuditLog();

    public CompletableFuture<User> getUser();

    public Optional<String> getReason();

    public AuditLogActionType getType();

    public Optional<AuditLogEntryTarget> getTarget();

    public List<AuditLogChange<?>> getChanges();

    default public CompletableFuture<AuditLog> getAuditLogBefore(int limit) {
        return this.getAuditLog().getServer().getAuditLogBefore(limit, this);
    }

    default public CompletableFuture<AuditLog> getAuditLogBefore(int limit, AuditLogActionType type) {
        return this.getAuditLog().getServer().getAuditLogBefore(limit, this, type);
    }
}

