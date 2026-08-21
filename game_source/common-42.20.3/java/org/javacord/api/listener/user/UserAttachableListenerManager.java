/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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

public interface UserAttachableListenerManager {
    public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener var1);

    public List<InteractionCreateListener> getInteractionCreateListeners();

    public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener var1);

    public List<SlashCommandCreateListener> getSlashCommandCreateListeners();

    public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener var1);

    public List<AutocompleteCreateListener> getAutocompleteCreateListeners();

    public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener var1);

    public List<ModalSubmitListener> getModalSubmitListeners();

    public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener var1);

    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners();

    public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener var1);

    public List<MessageComponentCreateListener> getMessageComponentCreateListeners();

    public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener var1);

    public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners();

    public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener var1);

    public List<SelectMenuChooseListener> getSelectMenuChooseListeners();

    public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener var1);

    public List<ButtonClickListener> getButtonClickListeners();

    public ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener var1);

    public List<ServerMemberJoinListener> getServerMemberJoinListeners();

    public ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(ServerMemberLeaveListener var1);

    public List<ServerMemberLeaveListener> getServerMemberLeaveListeners();

    public ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener var1);

    public List<ServerMemberBanListener> getServerMemberBanListeners();

    public ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener var1);

    public List<ServerMemberUnbanListener> getServerMemberUnbanListeners();

    public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener var1);

    public List<UserRoleRemoveListener> getUserRoleRemoveListeners();

    public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener var1);

    public List<UserRoleAddListener> getUserRoleAddListeners();

    public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener var1);

    public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners();

    public ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(ServerVoiceChannelMemberLeaveListener var1);

    public List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners();

    public ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(ServerVoiceChannelMemberJoinListener var1);

    public List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners();

    public ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(PrivateChannelDeleteListener var1);

    public List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners();

    public ListenerManager<PrivateChannelCreateListener> addPrivateChannelCreateListener(PrivateChannelCreateListener var1);

    public List<PrivateChannelCreateListener> getPrivateChannelCreateListeners();

    public ListenerManager<UserChangeDeafenedListener> addUserChangeDeafenedListener(UserChangeDeafenedListener var1);

    public List<UserChangeDeafenedListener> getUserChangeDeafenedListeners();

    public ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(UserChangeNicknameListener var1);

    public List<UserChangeNicknameListener> getUserChangeNicknameListeners();

    public ListenerManager<UserChangePendingListener> addUserChangePendingListener(UserChangePendingListener var1);

    public List<UserChangePendingListener> getUserChangePendingListeners();

    public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener var1);

    public List<UserStartTypingListener> getUserStartTypingListeners();

    public ListenerManager<UserChangeDiscriminatorListener> addUserChangeDiscriminatorListener(UserChangeDiscriminatorListener var1);

    public List<UserChangeDiscriminatorListener> getUserChangeDiscriminatorListeners();

    public ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener var1);

    public List<UserChangeStatusListener> getUserChangeStatusListeners();

    public ListenerManager<UserChangeServerAvatarListener> addUserChangeServerAvatarListener(UserChangeServerAvatarListener var1);

    public List<UserChangeServerAvatarListener> getUserChangeServerAvatarListeners();

    public ListenerManager<UserChangeSelfMutedListener> addUserChangeSelfMutedListener(UserChangeSelfMutedListener var1);

    public List<UserChangeSelfMutedListener> getUserChangeSelfMutedListeners();

    public ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener var1);

    public List<UserChangeNameListener> getUserChangeNameListeners();

    public ListenerManager<UserChangeTimeoutListener> addUserChangeTimeoutListener(UserChangeTimeoutListener var1);

    public List<UserChangeTimeoutListener> getUserChangeTimeoutListeners();

    public ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener var1);

    public List<UserChangeAvatarListener> getUserChangeAvatarListeners();

    public ListenerManager<UserChangeSelfDeafenedListener> addUserChangeSelfDeafenedListener(UserChangeSelfDeafenedListener var1);

    public List<UserChangeSelfDeafenedListener> getUserChangeSelfDeafenedListeners();

    public ListenerManager<UserChangeMutedListener> addUserChangeMutedListener(UserChangeMutedListener var1);

    public List<UserChangeMutedListener> getUserChangeMutedListeners();

    public ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener var1);

    public List<UserChangeActivityListener> getUserChangeActivityListeners();

    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener var1);

    public List<ReactionRemoveListener> getReactionRemoveListeners();

    public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener var1);

    public List<ReactionAddListener> getReactionAddListeners();

    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener var1);

    public List<MessageCreateListener> getMessageCreateListeners();

    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener var1);

    public List<MessageReplyListener> getMessageReplyListeners();

    public <T extends UserAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addUserAttachableListener(T var1);

    public <T extends UserAttachableListener & ObjectAttachableListener> void removeUserAttachableListener(T var1);

    public <T extends UserAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getUserAttachableListeners();

    public <T extends UserAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

