/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelCreateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelDeleteListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelMembersUpdateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelUpdateListener;
import org.javacord.api.listener.server.thread.ServerPrivateThreadJoinListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeArchiveTimestampListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeArchivedListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeAutoArchiveDurationListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeInvitableListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeLastMessageIdListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeLockedListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeMemberCountListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeMessageCountListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeRateLimitPerUserListener;
import org.javacord.api.listener.server.thread.ServerThreadChannelChangeTotalMessageSentListener;
import org.javacord.api.util.event.ListenerManager;

public interface ServerThreadChannelAttachableListenerManager {
    public ListenerManager<ServerThreadChannelChangeLastMessageIdListener> addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener var1);

    public List<ServerThreadChannelChangeLastMessageIdListener> getServerThreadChannelChangeLastMessageIdListeners();

    public ListenerManager<ServerThreadChannelChangeArchivedListener> addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener var1);

    public List<ServerThreadChannelChangeArchivedListener> getServerThreadChannelChangeArchivedListeners();

    public ListenerManager<ServerThreadChannelChangeMemberCountListener> addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener var1);

    public List<ServerThreadChannelChangeMemberCountListener> getServerThreadChannelChangeMemberCountListeners();

    public ListenerManager<ServerPrivateThreadJoinListener> addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener var1);

    public List<ServerPrivateThreadJoinListener> getServerPrivateThreadJoinListeners();

    public ListenerManager<ServerThreadChannelChangeInvitableListener> addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener var1);

    public List<ServerThreadChannelChangeInvitableListener> getServerThreadChannelChangeInvitableListeners();

    public ListenerManager<ServerThreadChannelChangeAutoArchiveDurationListener> addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener var1);

    public List<ServerThreadChannelChangeAutoArchiveDurationListener> getServerThreadChannelChangeAutoArchiveDurationListeners();

    public ListenerManager<ServerThreadChannelChangeRateLimitPerUserListener> addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener var1);

    public List<ServerThreadChannelChangeRateLimitPerUserListener> getServerThreadChannelChangeRateLimitPerUserListeners();

    public ListenerManager<ServerThreadChannelChangeLockedListener> addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener var1);

    public List<ServerThreadChannelChangeLockedListener> getServerThreadChannelChangeLockedListeners();

    public ListenerManager<ServerThreadChannelChangeArchiveTimestampListener> addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener var1);

    public List<ServerThreadChannelChangeArchiveTimestampListener> getServerThreadChannelChangeArchiveTimestampListeners();

    public ListenerManager<ServerThreadChannelChangeTotalMessageSentListener> addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener var1);

    public List<ServerThreadChannelChangeTotalMessageSentListener> getServerThreadChannelChangeTotalMessageSentListeners();

    public ListenerManager<ServerThreadChannelChangeMessageCountListener> addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener var1);

    public List<ServerThreadChannelChangeMessageCountListener> getServerThreadChannelChangeMessageCountListeners();

    public ListenerManager<ServerThreadChannelUpdateListener> addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener var1);

    public List<ServerThreadChannelUpdateListener> getServerThreadChannelUpdateListeners();

    public ListenerManager<ServerThreadChannelMembersUpdateListener> addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener var1);

    public List<ServerThreadChannelMembersUpdateListener> getServerThreadChannelMembersUpdateListeners();

    public ListenerManager<ServerThreadChannelCreateListener> addServerThreadChannelCreateListener(ServerThreadChannelCreateListener var1);

    public List<ServerThreadChannelCreateListener> getServerThreadChannelCreateListeners();

    public ListenerManager<ServerThreadChannelDeleteListener> addServerThreadChannelDeleteListener(ServerThreadChannelDeleteListener var1);

    public List<ServerThreadChannelDeleteListener> getServerThreadChannelDeleteListeners();

    public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerThreadChannelAttachableListener(T var1);

    public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> void removeServerThreadChannelAttachableListener(T var1);

    public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerThreadChannelAttachableListeners();

    public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

