/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import java.util.Set;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.event.user.OptionalUserEvent;

public interface UserChangeActivityEvent
extends OptionalUserEvent {
    public Set<Activity> getOldActivities();

    public Set<Activity> getNewActivities();
}

