/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerTextChannelUpdater;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerThreadChannelBuilder;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.ArchivedThreads;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListenerManager;

public interface ServerTextChannel
extends RegularServerChannel,
TextableRegularServerChannel,
ServerTextChannelAttachableListenerManager {
    @Override
    default public String getMentionTag() {
        return "<#" + this.getIdAsString() + ">";
    }

    public int getDefaultAutoArchiveDuration();

    public String getTopic();

    @Override
    default public ServerTextChannelUpdater createUpdater() {
        return new ServerTextChannelUpdater(this);
    }

    default public CompletableFuture<Void> updateTopic(String topic) {
        return this.createUpdater().setTopic(topic).update();
    }

    default public CompletableFuture<Void> updateNsfwFlag(boolean nsfw) {
        return this.createUpdater().setNsfwFlag(nsfw).update();
    }

    @Override
    default public CompletableFuture<Void> updateCategory(ChannelCategory category) {
        return this.createUpdater().setCategory(category).update();
    }

    @Override
    default public CompletableFuture<Void> removeCategory() {
        return this.createUpdater().removeCategory().update();
    }

    public int getSlowmodeDelayInSeconds();

    default public boolean hasSlowmode() {
        return this.getSlowmodeDelayInSeconds() != 0;
    }

    default public CompletableFuture<Void> updateSlowmodeDelayInSeconds(int delay) {
        return this.createUpdater().setSlowmodeDelayInSeconds(delay).update();
    }

    default public CompletableFuture<Void> unsetSlowmode() {
        return this.createUpdater().unsetSlowmode().update();
    }

    default public Optional<ServerTextChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getTextChannelById(this.getId()));
    }

    @Override
    default public CompletableFuture<ServerTextChannel> getLatestInstance() {
        Optional<ServerTextChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture<ServerTextChannel> result = new CompletableFuture<ServerTextChannel>();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }

    default public CompletableFuture<ServerThreadChannel> createThreadForMessage(Message message, String name, AutoArchiveDuration autoArchiveDuration) {
        return this.createThreadForMessage(message, name, autoArchiveDuration.asInt());
    }

    default public CompletableFuture<ServerThreadChannel> createThreadForMessage(Message message, String name, Integer autoArchiveDuration) {
        return new ServerThreadChannelBuilder(message, name).setAutoArchiveDuration(autoArchiveDuration).create();
    }

    default public CompletableFuture<ServerThreadChannel> createThread(ChannelType channelType, String name, Integer autoArchiveDuration) {
        return this.createThread(channelType, name, autoArchiveDuration, null);
    }

    default public CompletableFuture<ServerThreadChannel> createThread(ChannelType channelType, String name, AutoArchiveDuration autoArchiveDuration) {
        return this.createThread(channelType, name, autoArchiveDuration.asInt(), null);
    }

    default public CompletableFuture<ServerThreadChannel> createThread(ChannelType channelType, String name, Integer autoArchiveDuration, Boolean inviteable) {
        return new ServerThreadChannelBuilder(this, channelType, name).setAutoArchiveDuration(autoArchiveDuration).setInvitableFlag(inviteable).create();
    }

    default public CompletableFuture<ServerThreadChannel> createThread(ChannelType channelType, String name, AutoArchiveDuration autoArchiveDuration, Boolean inviteable) {
        return this.createThread(channelType, name, autoArchiveDuration.asInt(), inviteable);
    }

    default public CompletableFuture<ArchivedThreads> getPublicArchivedThreads() {
        return this.getPublicArchivedThreads(null, null);
    }

    default public CompletableFuture<ArchivedThreads> getPublicArchivedThreads(long before) {
        return this.getPublicArchivedThreads(before, null);
    }

    default public CompletableFuture<ArchivedThreads> getPublicArchivedThreads(int limit) {
        return this.getPublicArchivedThreads(null, limit);
    }

    public CompletableFuture<ArchivedThreads> getPublicArchivedThreads(Long var1, Integer var2);

    default public CompletableFuture<ArchivedThreads> getPrivateArchivedThreads() {
        return this.getPrivateArchivedThreads(null, null);
    }

    default public CompletableFuture<ArchivedThreads> getPrivateArchivedThreads(long before) {
        return this.getPrivateArchivedThreads(before, null);
    }

    default public CompletableFuture<ArchivedThreads> getPrivateArchivedThreads(int limit) {
        return this.getPrivateArchivedThreads(null, limit);
    }

    public CompletableFuture<ArchivedThreads> getPrivateArchivedThreads(Long var1, Integer var2);

    default public CompletableFuture<ArchivedThreads> getJoinedPrivateArchivedThreads() {
        return this.getJoinedPrivateArchivedThreads(null, null);
    }

    default public CompletableFuture<ArchivedThreads> getJoinedPrivateArchivedThreads(long before) {
        return this.getJoinedPrivateArchivedThreads(before, null);
    }

    default public CompletableFuture<ArchivedThreads> getJoinedPrivateArchivedThreads(int limit) {
        return this.getJoinedPrivateArchivedThreads(null, limit);
    }

    public CompletableFuture<ArchivedThreads> getJoinedPrivateArchivedThreads(Long var1, Integer var2);
}

