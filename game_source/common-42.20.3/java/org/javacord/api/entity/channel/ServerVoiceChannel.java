/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelUpdater;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListenerManager;

public interface ServerVoiceChannel
extends RegularServerChannel,
VoiceChannel,
TextableRegularServerChannel,
ServerVoiceChannelAttachableListenerManager {
    default public CompletableFuture<AudioConnection> connect() {
        return this.connect(false, true);
    }

    public CompletableFuture<AudioConnection> connect(boolean var1, boolean var2);

    default public CompletableFuture<Void> disconnect() {
        return this.getServer().getAudioConnection().filter(audioConnection -> this.equals(audioConnection.getChannel())).map(AudioConnection::close).orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    @Override
    public boolean isNsfw();

    public int getBitrate();

    public Optional<Integer> getUserLimit();

    public Set<Long> getConnectedUserIds();

    public Set<User> getConnectedUsers();

    public boolean isConnected(long var1);

    default public boolean isConnected(User user) {
        return this.isConnected(user.getId());
    }

    default public boolean isPrioritySpeaker(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.PRIORITY_SPEAKER) || this.hasPermissions(user, PermissionType.PRIORITY_SPEAKER, PermissionType.CONNECT);
    }

    default public boolean canConnect(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.CONNECT);
    }

    default public boolean canYouConnect() {
        return this.canConnect(this.getApi().getYourself());
    }

    default public boolean canMuteUsers(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.MUTE_MEMBERS, PermissionType.CONNECT);
    }

    default public boolean canYouMuteUsers() {
        return this.canMuteUsers(this.getApi().getYourself());
    }

    default public boolean canSpeak(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.SPEAK, PermissionType.CONNECT);
    }

    default public boolean canYouSpeak() {
        return this.canSpeak(this.getApi().getYourself());
    }

    default public boolean canUseVideo(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.STREAM, PermissionType.CONNECT);
    }

    default public boolean canYouUseVideo() {
        return this.canUseVideo(this.getApi().getYourself());
    }

    default public boolean canMoveUsers(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.MOVE_MEMBERS, PermissionType.CONNECT);
    }

    default public boolean canYouMoveUsers() {
        return this.canMoveUsers(this.getApi().getYourself());
    }

    default public boolean canUseVoiceActivation(User user) {
        return this.hasAnyPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.USE_VOICE_ACTIVITY, PermissionType.CONNECT);
    }

    default public boolean canYouUseVoiceActivation() {
        return this.canUseVoiceActivation(this.getApi().getYourself());
    }

    default public boolean canDeafenUsers(User user) {
        return this.hasPermission(user, PermissionType.ADMINISTRATOR) || this.hasPermissions(user, PermissionType.DEAFEN_MEMBERS, PermissionType.CONNECT);
    }

    default public boolean canYouDeafenUsers() {
        return this.canDeafenUsers(this.getApi().getYourself());
    }

    @Override
    default public ServerVoiceChannelUpdater createUpdater() {
        return new ServerVoiceChannelUpdater(this);
    }

    default public CompletableFuture<Void> updateBitrate(int bitrate) {
        return this.createUpdater().setBitrate(bitrate).update();
    }

    default public CompletableFuture<Void> updateUserLimit(int userLimit) {
        return this.createUpdater().setUserLimit(userLimit).update();
    }

    default public CompletableFuture<Void> removeUserLimit() {
        return this.createUpdater().removeUserLimit().update();
    }

    @Override
    default public CompletableFuture<Void> updateCategory(ChannelCategory category) {
        return this.createUpdater().setCategory(category).update();
    }

    @Override
    default public CompletableFuture<Void> removeCategory() {
        return this.createUpdater().removeCategory().update();
    }

    default public CompletableFuture<Void> updateNsfwFlag(boolean nsfw) {
        return this.createUpdater().setNsfw(nsfw).update();
    }

    default public Optional<? extends ServerVoiceChannel> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getVoiceChannelById(this.getId()));
    }

    @Override
    default public CompletableFuture<? extends ServerVoiceChannel> getLatestInstance() {
        Optional<? extends ServerVoiceChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

