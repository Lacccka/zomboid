/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListenerManager;
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
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalServerThreadChannelAttachableListenerManager
extends ServerThreadChannelAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<ServerThreadChannelChangeLastMessageIdListener> addServerThreadChannelChangeLastMessageIdListener(ServerThreadChannelChangeLastMessageIdListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeLastMessageIdListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeLastMessageIdListener> getServerThreadChannelChangeLastMessageIdListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeLastMessageIdListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeArchivedListener> addServerThreadChannelChangeArchivedListener(ServerThreadChannelChangeArchivedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeArchivedListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeArchivedListener> getServerThreadChannelChangeArchivedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeArchivedListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeMemberCountListener> addServerThreadChannelChangeMemberCountListener(ServerThreadChannelChangeMemberCountListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeMemberCountListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeMemberCountListener> getServerThreadChannelChangeMemberCountListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeMemberCountListener.class);
    }

    @Override
    default public ListenerManager<ServerPrivateThreadJoinListener> addServerPrivateThreadJoinListener(ServerPrivateThreadJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerPrivateThreadJoinListener.class, listener);
    }

    @Override
    default public List<ServerPrivateThreadJoinListener> getServerPrivateThreadJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerPrivateThreadJoinListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeInvitableListener> addServerThreadChannelChangeInvitableListener(ServerThreadChannelChangeInvitableListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeInvitableListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeInvitableListener> getServerThreadChannelChangeInvitableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeInvitableListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeAutoArchiveDurationListener> addServerThreadChannelChangeAutoArchiveDurationListener(ServerThreadChannelChangeAutoArchiveDurationListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeAutoArchiveDurationListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeAutoArchiveDurationListener> getServerThreadChannelChangeAutoArchiveDurationListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeAutoArchiveDurationListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeRateLimitPerUserListener> addServerThreadChannelChangeRateLimitPerUserListener(ServerThreadChannelChangeRateLimitPerUserListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeRateLimitPerUserListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeRateLimitPerUserListener> getServerThreadChannelChangeRateLimitPerUserListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeRateLimitPerUserListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeLockedListener> addServerThreadChannelChangeLockedListener(ServerThreadChannelChangeLockedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeLockedListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeLockedListener> getServerThreadChannelChangeLockedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeLockedListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeArchiveTimestampListener> addServerThreadChannelChangeArchiveTimestampListener(ServerThreadChannelChangeArchiveTimestampListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeArchiveTimestampListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeArchiveTimestampListener> getServerThreadChannelChangeArchiveTimestampListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeArchiveTimestampListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeTotalMessageSentListener> addServerThreadChannelChangeTotalMessageSentListener(ServerThreadChannelChangeTotalMessageSentListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeTotalMessageSentListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeTotalMessageSentListener> getServerThreadChannelChangeTotalMessageSentListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeTotalMessageSentListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelChangeMessageCountListener> addServerThreadChannelChangeMessageCountListener(ServerThreadChannelChangeMessageCountListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeMessageCountListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelChangeMessageCountListener> getServerThreadChannelChangeMessageCountListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelChangeMessageCountListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelUpdateListener> addServerThreadChannelUpdateListener(ServerThreadChannelUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelUpdateListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelUpdateListener> getServerThreadChannelUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelMembersUpdateListener> addServerThreadChannelMembersUpdateListener(ServerThreadChannelMembersUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelMembersUpdateListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelMembersUpdateListener> getServerThreadChannelMembersUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelMembersUpdateListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelCreateListener> addServerThreadChannelCreateListener(ServerThreadChannelCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelCreateListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelCreateListener> getServerThreadChannelCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelCreateListener.class);
    }

    @Override
    default public ListenerManager<ServerThreadChannelDeleteListener> addServerThreadChannelDeleteListener(ServerThreadChannelDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), ServerThreadChannelDeleteListener.class, listener);
    }

    @Override
    default public List<ServerThreadChannelDeleteListener> getServerThreadChannelDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId(), ServerThreadChannelDeleteListener.class);
    }

    @Override
    default public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addServerThreadChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerThreadChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(ServerThreadChannel.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> void removeServerThreadChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(ServerThreadChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerThreadChannel.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getServerThreadChannelAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(ServerThreadChannel.class, this.getId());
    }

    @Override
    default public <T extends ServerThreadChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(ServerThreadChannel.class, this.getId(), listenerClass, listener);
    }
}

