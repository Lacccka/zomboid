/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.user.User;
import org.javacord.core.util.ImmutableToJavaMapper;
import org.javacord.core.util.cache.Cache;

public class UserCache {
    private static final String ID_INDEX_NAME = "id";
    private static final UserCache EMPTY_CACHE = new UserCache(Cache.empty().addIndex("id", DiscordEntity::getId));
    private final Cache<User> cache;

    private UserCache(Cache<User> cache) {
        this.cache = cache;
    }

    public static UserCache empty() {
        return EMPTY_CACHE;
    }

    public UserCache addUser(User user) {
        return new UserCache(this.cache.addElement(user));
    }

    public UserCache removeUser(User user) {
        return new UserCache(this.cache.removeElement(user));
    }

    public Set<User> getUsers() {
        return ImmutableToJavaMapper.mapToJava(this.cache.getAll());
    }

    public Optional<User> getUserById(long id) {
        return this.cache.findAnyByIndex(ID_INDEX_NAME, id);
    }
}

