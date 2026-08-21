/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListenerManager;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListenerManager;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.interaction.AutocompleteCreateListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.InteractionCreateListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.ModalSubmitListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.interaction.UserContextMenuCommandListener;
import org.javacord.api.listener.message.CachedMessagePinListener;
import org.javacord.api.listener.message.CachedMessageUnpinListener;
import org.javacord.api.listener.message.ChannelPinsUpdateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.javacord.api.util.event.ListenerManager;

public interface TextChannelAttachableListenerManager
extends ServerThreadChannelAttachableListenerManager,
ChannelAttachableListenerManager {
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

    public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener var1);

    public List<UserStartTypingListener> getUserStartTypingListeners();

    public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener var1);

    public List<MessageEditListener> getMessageEditListeners();

    public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener var1);

    public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners();

    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener var1);

    public List<ReactionRemoveListener> getReactionRemoveListeners();

    public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener var1);

    public List<ReactionAddListener> getReactionAddListeners();

    public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener var1);

    public List<ReactionRemoveAllListener> getReactionRemoveAllListeners();

    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener var1);

    public List<MessageCreateListener> getMessageCreateListeners();

    public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener var1);

    public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners();

    public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener var1);

    public List<CachedMessagePinListener> getCachedMessagePinListeners();

    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener var1);

    public List<MessageReplyListener> getMessageReplyListeners();

    public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener var1);

    public List<MessageDeleteListener> getMessageDeleteListeners();

    public <T extends TextChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends TextChannelAttachableListener>> addTextChannelAttachableListener(T var1);

    public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeTextChannelAttachableListener(T var1);

    public <T extends TextChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getTextChannelAttachableListeners();

    @Override
    public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

