/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerThreadChannelUpdater;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.channel.thread.ThreadMetadata;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListenerManager;

public interface ServerThreadChannel
extends ServerChannel,
TextChannel,
Mentionable,
ServerThreadChannelAttachableListenerManager {
    @Override
    default public boolean canSee(User user) {
        return this.getParent().canSee(user) && (this.isPublic() || this.isPrivate() && this.getMembers().stream().anyMatch(threadMember -> threadMember.getUserId() == user.getId()));
    }

    @Override
    default public boolean canWrite(User user) {
        return this.canSee(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.SEND_MESSAGES_IN_THREADS);
    }

    @Override
    default public boolean canUseExternalEmojis(User user) {
        return this.canWrite(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.USE_EXTERNAL_EMOJIS);
    }

    @Override
    default public boolean canEmbedLinks(User user) {
        return this.canWrite(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.EMBED_LINKS);
    }

    @Override
    default public boolean canReadMessageHistory(User user) {
        return this.canSee(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.READ_MESSAGE_HISTORY);
    }

    @Override
    default public boolean canUseTts(User user) {
        return this.canWrite(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.SEND_TTS_MESSAGES);
    }

    @Override
    default public boolean canAttachFiles(User user) {
        return this.canWrite(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.ATTACH_FILE);
    }

    @Override
    default public boolean canAddNewReactions(User user) {
        return this.canSee(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.ADD_REACTIONS);
    }

    @Override
    default public boolean canManageMessages(User user) {
        return this.canSee(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
    }

    @Override
    default public boolean canMentionEveryone(User user) {
        return this.canWrite(user) && this.getParent().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MENTION_EVERYONE);
    }

    public RegularServerChannel getParent();

    public int getMessageCount();

    public int getMemberCount();

    public long getLastMessageId();

    public int getTotalNumberOfMessagesSent();

    public int getRateLimitPerUser();

    public ThreadMetadata getMetadata();

    default public boolean isPrivate() {
        return this.getType() == ChannelType.SERVER_PRIVATE_THREAD;
    }

    default public boolean isPublic() {
        return this.getType() == ChannelType.SERVER_PUBLIC_THREAD;
    }

    public long getOwnerId();

    default public CompletableFuture<User> requestOwner() {
        return this.getApi().getUserById(this.getOwnerId());
    }

    public Set<ThreadMember> getMembers();

    default public Optional<ServerThreadChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getThreadChannelById(this.getId()));
    }

    @Override
    default public CompletableFuture<ServerThreadChannel> getLatestInstance() {
        Optional<ServerThreadChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture<ServerThreadChannel> result = new CompletableFuture<ServerThreadChannel>();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }

    default public CompletableFuture<Void> addThreadMember(User user) {
        return this.addThreadMember(user.getId());
    }

    public CompletableFuture<Void> addThreadMember(long var1);

    default public CompletableFuture<Void> removeThreadMember(User user) {
        return this.removeThreadMember(user.getId());
    }

    public CompletableFuture<Void> removeThreadMember(long var1);

    default public CompletableFuture<Void> joinThread() {
        return this.getServer().joinServerThreadChannel(this.getId());
    }

    default public CompletableFuture<Void> leaveThread() {
        return this.getServer().leaveServerThreadChannel(this.getId());
    }

    public CompletableFuture<ThreadMember> requestThreadMemberById(long var1);

    default public CompletableFuture<ThreadMember> requestThreadMemberById(String userId) {
        return this.requestThreadMemberById(Long.parseLong(userId));
    }

    @Deprecated
    default public CompletableFuture<Set<ThreadMember>> getThreadMembers() {
        return this.requestThreadMembers();
    }

    public CompletableFuture<Set<ThreadMember>> requestThreadMembers();

    @Override
    default public ServerThreadChannelUpdater createUpdater() {
        return new ServerThreadChannelUpdater(this);
    }
}

