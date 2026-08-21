/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.thread;

import java.time.Instant;
import java.util.Optional;

public interface ThreadMetadata {
    public boolean isArchived();

    public int getAutoArchiveDuration();

    public boolean isLocked();

    public Instant getArchiveTimestamp();

    public Optional<Boolean> isInvitable();

    public Optional<Instant> getCreationTimestamp();
}

