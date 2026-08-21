/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.auditlog;

import java.util.Objects;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.auditlog.AuditLogEntryTarget;

public class AuditLogEntryTargetImpl
implements AuditLogEntryTarget {
    private final long id;
    private final AuditLogEntry auditLogEntry;

    public AuditLogEntryTargetImpl(long id, AuditLogEntry auditLogEntry) {
        this.id = id;
        this.auditLogEntry = auditLogEntry;
    }

    @Override
    public DiscordApi getApi() {
        return this.auditLogEntry.getApi();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public AuditLogEntry getAuditLogEntry() {
        return this.auditLogEntry;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("AuditLogEntryTarget (id: %s)", this.getIdAsString());
    }
}

