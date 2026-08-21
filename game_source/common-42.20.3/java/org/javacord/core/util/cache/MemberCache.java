/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.server.Server;
import org.javacord.core.entity.user.Member;
import org.javacord.core.util.ImmutableToJavaMapper;
import org.javacord.core.util.cache.Cache;
import org.javacord.core.util.cache.UserCache;

public class MemberCache {
    private static final String ID_INDEX_NAME = "id";
    private static final String SERVER_ID_INDEX_NAME = "server-id";
    private static final String ID_AND_SERVER_ID_INDEX_NAME = "server-id | type";
    private static final String MEMBER_SERVER_MEMBER_ID_INDEX_NAME = "ms > member-id";
    private static final String MEMBER_SERVER_MEMBER_ID_SERVER_ID_INDEX_NAME = "ms > member-id | server-id";
    private static final MemberCache EMPTY_CACHE = new MemberCache(Cache.empty().addIndex("id", DiscordEntity::getId).addIndex("server-id", member -> member.getServer().getId()).addIndex("server-id | type", member -> Tuple.of(member.getId(), member.getServer().getId())), UserCache.empty(), Cache.empty().addIndex("ms > member-id", tuple -> ((Member)tuple._1()).getId()).addIndex("ms > member-id | server-id", tuple -> Tuple.of(((Member)tuple._1).getId(), ((Server)tuple._2).getId())));
    private final Cache<Tuple2<Member, Server>> memberServerCache;
    private final Cache<Member> cache;
    private final UserCache userCache;

    private MemberCache(Cache<Member> cache, UserCache userCache, Cache<Tuple2<Member, Server>> memberServerCache) {
        this.cache = cache;
        this.userCache = userCache;
        this.memberServerCache = memberServerCache;
    }

    public static MemberCache empty() {
        return EMPTY_CACHE;
    }

    public MemberCache addMember(Member member) {
        return new MemberCache(this.cache.addElement(member), this.userCache.getUserById(member.getId()).map(this.userCache::removeUser).orElse(this.userCache).addUser(member.getUser()), this.memberServerCache.addElement(Tuple.of(member, member.getServer())));
    }

    public MemberCache removeMember(Member member) {
        if (member == null) {
            return this;
        }
        Tuple2 memberServerTuple = this.memberServerCache.findAnyByIndex(MEMBER_SERVER_MEMBER_ID_SERVER_ID_INDEX_NAME, Tuple.of(member.getId(), member.getServer().getId())).orElse(null);
        return new MemberCache(this.cache.removeElement(member), this.userCache.getUserById(member.getId()).filter(user -> this.getMembersById(user.getId()).size() <= 1).map(this.userCache::removeUser).orElse(this.userCache), memberServerTuple == null ? this.memberServerCache : this.memberServerCache.removeElement(memberServerTuple));
    }

    public Set<Server> getServers(long userId) {
        return ImmutableToJavaMapper.mapToJava(this.memberServerCache.findByIndex(MEMBER_SERVER_MEMBER_ID_INDEX_NAME, userId).map(tuple -> (Server)tuple._2));
    }

    public UserCache getUserCache() {
        return this.userCache;
    }

    public Set<Member> getMembers() {
        return ImmutableToJavaMapper.mapToJava(this.cache.getAll());
    }

    public Set<Member> getMembersById(long id) {
        return ImmutableToJavaMapper.mapToJava(this.cache.findByIndex(ID_INDEX_NAME, id));
    }

    public Set<Member> getMembersByServer(long serverId) {
        return ImmutableToJavaMapper.mapToJava(this.cache.findByIndex(SERVER_ID_INDEX_NAME, serverId));
    }

    public Optional<Member> getMemberByIdAndServer(long id, long serverId) {
        return this.cache.findAnyByIndex(ID_AND_SERVER_ID_INDEX_NAME, Tuple.of(id, serverId));
    }
}

