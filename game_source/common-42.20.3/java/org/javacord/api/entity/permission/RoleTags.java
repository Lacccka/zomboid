/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import java.util.Optional;

public interface RoleTags {
    public Optional<Long> getBotId();

    public Optional<Long> getIntegrationId();

    public boolean isPremiumSubscriptionRole();
}

