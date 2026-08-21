/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.auditlog;

import java.util.Optional;
import org.javacord.api.entity.auditlog.AuditLogChange;
import org.javacord.api.entity.auditlog.AuditLogChangeType;

public class AuditLogChangeImpl<T>
implements AuditLogChange<T> {
    private final AuditLogChangeType type;
    private final T oldValue;
    private final T newValue;

    public AuditLogChangeImpl(AuditLogChangeType type, T oldValue, T newValue) {
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public AuditLogChangeType getType() {
        return this.type;
    }

    @Override
    public Optional<T> getOldValue() {
        return Optional.ofNullable(this.oldValue);
    }

    @Override
    public Optional<T> getNewValue() {
        return Optional.ofNullable(this.newValue);
    }
}

