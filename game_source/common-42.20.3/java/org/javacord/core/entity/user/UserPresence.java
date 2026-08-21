/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.user;

import io.vavr.collection.Map;
import java.util.Collections;
import java.util.Set;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.user.UserStatus;

public class UserPresence {
    private final long userId;
    private final Set<Activity> activities;
    private final UserStatus status;
    private final Map<DiscordClient, UserStatus> clientStatus;

    public UserPresence(long userId, Set<Activity> activities, UserStatus status, Map<DiscordClient, UserStatus> clientStatus) {
        this.userId = userId;
        this.activities = activities;
        this.status = status;
        this.clientStatus = clientStatus;
    }

    public long getUserId() {
        return this.userId;
    }

    public UserPresence setActivities(Set<Activity> activities) {
        return new UserPresence(this.userId, activities, this.status, this.clientStatus);
    }

    public Set<Activity> getActivities() {
        return Collections.unmodifiableSet(this.activities);
    }

    public UserPresence setStatus(UserStatus status) {
        return new UserPresence(this.userId, this.activities, status, this.clientStatus);
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public UserPresence setClientStatus(Map<DiscordClient, UserStatus> clientStatus) {
        return new UserPresence(this.userId, this.activities, this.status, clientStatus);
    }

    public Map<DiscordClient, UserStatus> getClientStatus() {
        return this.clientStatus;
    }
}

