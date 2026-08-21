/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.listener.channel.user.PrivateChannelCreateListener;
import org.javacord.api.listener.channel.user.PrivateChannelDeleteListener;
import org.javacord.api.listener.interaction.AutocompleteCreateListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.InteractionCreateListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.ModalSubmitListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.interaction.UserContextMenuCommandListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import org.javacord.api.listener.user.UserAttachableListener;
import org.javacord.api.listener.user.UserAttachableListenerManager;
import org.javacord.api.listener.user.UserChangeActivityListener;
import org.javacord.api.listener.user.UserChangeAvatarListener;
import org.javacord.api.listener.user.UserChangeDeafenedListener;
import org.javacord.api.listener.user.UserChangeDiscriminatorListener;
import org.javacord.api.listener.user.UserChangeMutedListener;
import org.javacord.api.listener.user.UserChangeNameListener;
import org.javacord.api.listener.user.UserChangeNicknameListener;
import org.javacord.api.listener.user.UserChangePendingListener;
import org.javacord.api.listener.user.UserChangeSelfDeafenedListener;
import org.javacord.api.listener.user.UserChangeSelfMutedListener;
import org.javacord.api.listener.user.UserChangeServerAvatarListener;
import org.javacord.api.listener.user.UserChangeStatusListener;
import org.javacord.api.listener.user.UserChangeTimeoutListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalUserAttachableListenerManager
extends UserAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), InteractionCreateListener.class, listener);
    }

    @Override
    default public List<InteractionCreateListener> getInteractionCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), InteractionCreateListener.class);
    }

    @Override
    default public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), SlashCommandCreateListener.class, listener);
    }

    @Override
    default public List<SlashCommandCreateListener> getSlashCommandCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), SlashCommandCreateListener.class);
    }

    @Override
    default public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), AutocompleteCreateListener.class, listener);
    }

    @Override
    default public List<AutocompleteCreateListener> getAutocompleteCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), AutocompleteCreateListener.class);
    }

    @Override
    default public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ModalSubmitListener.class, listener);
    }

    @Override
    default public List<ModalSubmitListener> getModalSubmitListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ModalSubmitListener.class);
    }

    @Override
    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), MessageContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), MessageContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), MessageComponentCreateListener.class, listener);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), MessageComponentCreateListener.class);
    }

    @Override
    default public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), SelectMenuChooseListener.class, listener);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), SelectMenuChooseListener.class);
    }

    @Override
    default public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ButtonClickListener.class, listener);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ButtonClickListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerMemberJoinListener.class, listener);
    }

    @Override
    default public List<ServerMemberJoinListener> getServerMemberJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerMemberJoinListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerMemberLeaveListener.class, listener);
    }

    @Override
    default public List<ServerMemberLeaveListener> getServerMemberLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerMemberLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerMemberBanListener.class, listener);
    }

    @Override
    default public List<ServerMemberBanListener> getServerMemberBanListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerMemberBanListener.class);
    }

    @Override
    default public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerMemberUnbanListener.class, listener);
    }

    @Override
    default public List<ServerMemberUnbanListener> getServerMemberUnbanListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerMemberUnbanListener.class);
    }

    @Override
    default public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserRoleRemoveListener.class, listener);
    }

    @Override
    default public List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserRoleRemoveListener.class);
    }

    @Override
    default public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserRoleAddListener.class, listener);
    }

    @Override
    default public List<UserRoleAddListener> getUserRoleAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserRoleAddListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerVoiceChannelMemberLeaveListener.class);
    }

    @Override
    default public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ServerVoiceChannelMemberJoinListener.class, listener);
    }

    @Override
    default public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ServerVoiceChannelMemberJoinListener.class);
    }

    @Override
    default public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), PrivateChannelDeleteListener.class, listener);
    }

    @Override
    default public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), PrivateChannelDeleteListener.class);
    }

    @Override
    default public ListenerManager<PrivateChannelCreateListener> addPrivateChannelCreateListener(PrivateChannelCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), PrivateChannelCreateListener.class, listener);
    }

    @Override
    default public List<PrivateChannelCreateListener> getPrivateChannelCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), PrivateChannelCreateListener.class);
    }

    @Override
    default public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeDeafenedListener.class, listener);
    }

    @Override
    default public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeDeafenedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeNicknameListener.class, listener);
    }

    @Override
    default public List<UserChangeNicknameListener> getUserChangeNicknameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeNicknameListener.class);
    }

    @Override
    default public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangePendingListener.class, listener);
    }

    @Override
    default public List<UserChangePendingListener> getUserChangePendingListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangePendingListener.class);
    }

    @Override
    default public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserStartTypingListener.class, listener);
    }

    @Override
    default public List<UserStartTypingListener> getUserStartTypingListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserStartTypingListener.class);
    }

    @Override
    default public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeDiscriminatorListener.class, listener);
    }

    @Override
    default public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeDiscriminatorListener.class);
    }

    @Override
    default public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeStatusListener.class, listener);
    }

    @Override
    default public List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeStatusListener.class);
    }

    @Override
    default public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeServerAvatarListener.class, listener);
    }

    @Override
    default public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeServerAvatarListener.class);
    }

    @Override
    default public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeSelfMutedListener.class, listener);
    }

    @Override
    default public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeSelfMutedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeNameListener.class, listener);
    }

    @Override
    default public List<UserChangeNameListener> getUserChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeNameListener.class);
    }

    @Override
    default public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeTimeoutListener.class, listener);
    }

    @Override
    default public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeTimeoutListener.class);
    }

    @Override
    default public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeAvatarListener.class, listener);
    }

    @Override
    default public List<UserChangeAvatarListener> getUserChangeAvatarListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeAvatarListener.class);
    }

    @Override
    default public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeSelfDeafenedListener.class, listener);
    }

    @Override
    default public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeSelfDeafenedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeMutedListener.class, listener);
    }

    @Override
    default public List<UserChangeMutedListener> getUserChangeMutedListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeMutedListener.class);
    }

    @Override
    default public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), UserChangeActivityListener.class, listener);
    }

    @Override
    default public List<UserChangeActivityListener> getUserChangeActivityListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), UserChangeActivityListener.class);
    }

    @Override
    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ReactionRemoveListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ReactionRemoveListener.class);
    }

    @Override
    default public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), ReactionAddListener.class, listener);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), ReactionAddListener.class);
    }

    @Override
    default public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), MessageCreateListener.class, listener);
    }

    @Override
    default public List<MessageCreateListener> getMessageCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), MessageCreateListener.class);
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), MessageReplyListener.class, listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId(), MessageReplyListener.class);
    }

    @Override
    default public <T extends UserAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addUserAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(UserAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(User.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends UserAttachableListener & ObjectAttachableListener> void removeUserAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(UserAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(User.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends UserAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getUserAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(User.class, this.getId());
    }

    @Override
    default public <T extends UserAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(User.class, this.getId(), listenerClass, listener);
    }
}

