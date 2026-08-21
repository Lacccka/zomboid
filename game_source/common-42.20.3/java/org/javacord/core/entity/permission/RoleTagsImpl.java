/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.permission;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.permission.RoleTags;

public class RoleTagsImpl
implements RoleTags {
    private final Long botId;
    private final Long integrationId;
    private final boolean isPremiumSubscriptionRole;

    public RoleTagsImpl(JsonNode data) {
        this.botId = data.hasNonNull("bot_id") ? Long.valueOf(data.get("bot_id").asLong(0L)) : null;
        this.integrationId = data.hasNonNull("integration_id") ? Long.valueOf(data.get("integration_id").asLong(0L)) : null;
        this.isPremiumSubscriptionRole = data.has("premium_subscriber");
    }

    @Override
    public Optional<Long> getBotId() {
        return Optional.ofNullable(this.botId);
    }

    @Override
    public Optional<Long> getIntegrationId() {
        return Optional.ofNullable(this.integrationId);
    }

    @Override
    public boolean isPremiumSubscriptionRole() {
        return this.isPremiumSubscriptionRole;
    }
}

