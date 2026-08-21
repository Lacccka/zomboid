/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.auditlog;

import java.util.Optional;
import org.javacord.api.entity.auditlog.AuditLogChangeType;

public interface AuditLogChange<T> {
    public AuditLogChangeType getType();

    public Optional<T> getOldValue();

    public Optional<T> getNewValue();
}

