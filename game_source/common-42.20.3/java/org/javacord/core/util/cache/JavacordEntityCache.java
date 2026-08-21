/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import java.util.function.UnaryOperator;
import org.javacord.core.util.cache.ChannelCache;
import org.javacord.core.util.cache.MemberCache;
import org.javacord.core.util.cache.UserPresenceCache;

public class JavacordEntityCache {
    private static final JavacordEntityCache EMPTY_CACHE = new JavacordEntityCache(ChannelCache.empty(), MemberCache.empty(), UserPresenceCache.empty());
    private final ChannelCache channelCache;
    private final MemberCache memberCache;
    private final UserPresenceCache userPresenceCache;

    public static JavacordEntityCache empty() {
        return EMPTY_CACHE;
    }

    private JavacordEntityCache(ChannelCache channelCache, MemberCache memberCache, UserPresenceCache userPresenceCache) {
        this.channelCache = channelCache;
        this.memberCache = memberCache;
        this.userPresenceCache = userPresenceCache;
    }

    public ChannelCache getChannelCache() {
        return this.channelCache;
    }

    public JavacordEntityCache updateChannelCache(UnaryOperator<ChannelCache> mapper) {
        return this.setChannelCache((ChannelCache)mapper.apply(this.channelCache));
    }

    public JavacordEntityCache setChannelCache(ChannelCache channelCache) {
        return new JavacordEntityCache(channelCache, this.memberCache, this.userPresenceCache);
    }

    public MemberCache getMemberCache() {
        return this.memberCache;
    }

    public JavacordEntityCache updateMemberCache(UnaryOperator<MemberCache> mapper) {
        return this.setMemberCache((MemberCache)mapper.apply(this.memberCache));
    }

    public JavacordEntityCache setMemberCache(MemberCache memberCache) {
        return new JavacordEntityCache(this.channelCache, memberCache, this.userPresenceCache);
    }

    public UserPresenceCache getUserPresenceCache() {
        return this.userPresenceCache;
    }

    public JavacordEntityCache updateUserPresenceCache(UnaryOperator<UserPresenceCache> mapper) {
        return this.setUserPresenceCache((UserPresenceCache)mapper.apply(this.userPresenceCache));
    }

    public JavacordEntityCache setUserPresenceCache(UserPresenceCache userPresenceCache) {
        return new JavacordEntityCache(this.channelCache, this.memberCache, userPresenceCache);
    }
}

