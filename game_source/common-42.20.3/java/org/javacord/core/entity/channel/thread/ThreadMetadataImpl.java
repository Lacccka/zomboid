/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.javacord.api.entity.channel.thread.ThreadMetadata;

public class ThreadMetadataImpl
implements ThreadMetadata {
    private boolean isArchived;
    private int autoArchiveDuration;
    private boolean isLocked;
    private Instant archiveTimestamp;
    private final Instant creationTimestamp;
    private Boolean isInvitable;

    public ThreadMetadataImpl(JsonNode data) {
        this.autoArchiveDuration = data.get("auto_archive_duration").asInt();
        this.isArchived = data.get("archived").asBoolean();
        this.isLocked = data.get("locked").asBoolean();
        this.archiveTimestamp = OffsetDateTime.parse(data.get("archive_timestamp").asText()).toInstant();
        this.creationTimestamp = data.hasNonNull("creation_timestamp") ? OffsetDateTime.parse(data.get("creation_timestamp").asText()).toInstant() : null;
        this.isInvitable = data.hasNonNull("invitable") ? Boolean.valueOf(data.get("invitable").asBoolean()) : null;
    }

    @Override
    public boolean isArchived() {
        return this.isArchived;
    }

    @Override
    public int getAutoArchiveDuration() {
        return this.autoArchiveDuration;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public Instant getArchiveTimestamp() {
        return this.archiveTimestamp;
    }

    @Override
    public Optional<Boolean> isInvitable() {
        return Optional.ofNullable(this.isInvitable);
    }

    @Override
    public Optional<Instant> getCreationTimestamp() {
        return Optional.ofNullable(this.creationTimestamp);
    }

    public void setArchived(boolean archived) {
        this.isArchived = archived;
    }

    public void setAutoArchiveDuration(int autoArchiveDuration) {
        this.autoArchiveDuration = autoArchiveDuration;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public void setArchiveTimestamp(Instant archiveTimestamp) {
        this.archiveTimestamp = archiveTimestamp;
    }

    public void setInvitable(Boolean invitable) {
        this.isInvitable = invitable;
    }
}

