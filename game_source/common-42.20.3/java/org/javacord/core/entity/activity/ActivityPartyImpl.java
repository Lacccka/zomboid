/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;
import org.javacord.api.entity.activity.ActivityParty;

public class ActivityPartyImpl
implements ActivityParty {
    private final String id;
    private final Integer currentSize;
    private final Integer maximumSize;

    public ActivityPartyImpl(JsonNode data) {
        String string = this.id = data.has("id") ? data.get("id").asText(null) : null;
        if (data.has("size")) {
            this.currentSize = data.get("size").get(0).asInt();
            this.maximumSize = data.get("size").get(1).asInt();
        } else {
            this.currentSize = null;
            this.maximumSize = null;
        }
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(this.id);
    }

    @Override
    public Optional<Integer> getCurrentSize() {
        return Optional.ofNullable(this.currentSize);
    }

    @Override
    public Optional<Integer> getMaximumSize() {
        return Optional.ofNullable(this.maximumSize);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ActivityPartyImpl)) {
            return false;
        }
        ActivityPartyImpl otherParty = (ActivityPartyImpl)obj;
        return Objects.deepEquals(this.id, otherParty.id) && Objects.deepEquals(this.currentSize, otherParty.currentSize) && Objects.deepEquals(this.maximumSize, otherParty.maximumSize);
    }

    public int hashCode() {
        int hash = 42;
        int idHash = this.id == null ? 0 : this.id.hashCode();
        int currentSize = this.currentSize == null ? 0 : this.currentSize;
        int maximumSize = this.maximumSize == null ? 0 : this.maximumSize;
        hash = hash * 11 + idHash;
        hash = hash * 13 + currentSize;
        hash = hash * 17 + maximumSize;
        return hash;
    }
}

