/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import java.util.Optional;
import org.javacord.core.entity.user.UserPresence;
import org.javacord.core.util.cache.Cache;

public class UserPresenceCache {
    private static final String USER_ID_INDEX_NAME = "user-id";
    private static final UserPresenceCache EMPTY_CACHE = new UserPresenceCache(Cache.empty().addIndex("user-id", UserPresence::getUserId));
    private final Cache<UserPresence> cache;

    private UserPresenceCache(Cache<UserPresence> cache) {
        this.cache = cache;
    }

    public static UserPresenceCache empty() {
        return EMPTY_CACHE;
    }

    public UserPresenceCache addUserPresence(UserPresence presence) {
        return new UserPresenceCache(this.cache.addElement(presence));
    }

    public UserPresenceCache removeUserPresence(UserPresence presence) {
        if (presence == null) {
            return this;
        }
        return new UserPresenceCache(this.cache.removeElement(presence));
    }

    public Optional<UserPresence> getPresenceByUserId(long userId) {
        return this.cache.findAnyByIndex(USER_ID_INDEX_NAME, userId);
    }
}

