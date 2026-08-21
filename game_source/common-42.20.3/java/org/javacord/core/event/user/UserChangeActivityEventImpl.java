/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import java.util.Collections;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.event.user.UserChangeActivityEvent;
import org.javacord.core.event.user.OptionalUserEventImpl;

public class UserChangeActivityEventImpl
extends OptionalUserEventImpl
implements UserChangeActivityEvent {
    private final Set<Activity> newActivities;
    private final Set<Activity> oldActivities;

    public UserChangeActivityEventImpl(DiscordApi api, long userId, Set<Activity> newActivities, Set<Activity> oldActivities) {
        super(api, userId);
        this.newActivities = newActivities;
        this.oldActivities = oldActivities;
    }

    @Override
    public Set<Activity> getOldActivities() {
        return Collections.unmodifiableSet(this.oldActivities);
    }

    @Override
    public Set<Activity> getNewActivities() {
        return Collections.unmodifiableSet(this.newActivities);
    }
}

