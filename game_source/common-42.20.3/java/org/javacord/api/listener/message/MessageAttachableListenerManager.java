/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.message.CachedMessagePinListener;
import org.javacord.api.listener.message.CachedMessageUnpinListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;

public interface MessageAttachableListenerManager {
    public static ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(DiscordApi api, long messageId, MessageContextMenuCommandListener listener) {
        return api.getUncachedMessageUtil().addMessageContextMenuCommandListener(messageId, listener);
    }

    public static List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageContextMenuCommandListeners(messageId);
    }

    public static List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageContextMenuCommandListeners(messageId);
    }

    public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener var1);

    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners();

    public static ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(DiscordApi api, long messageId, MessageComponentCreateListener listener) {
        return api.getUncachedMessageUtil().addMessageComponentCreateListener(messageId, listener);
    }

    public static List<MessageComponentCreateListener> getMessageComponentCreateListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageComponentCreateListeners(messageId);
    }

    public static List<MessageComponentCreateListener> getMessageComponentCreateListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageComponentCreateListeners(messageId);
    }

    public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener var1);

    public List<MessageComponentCreateListener> getMessageComponentCreateListeners();

    public static ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(DiscordApi api, long messageId, SelectMenuChooseListener listener) {
        return api.getUncachedMessageUtil().addSelectMenuChooseListener(messageId, listener);
    }

    public static List<SelectMenuChooseListener> getSelectMenuChooseListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getSelectMenuChooseListeners(messageId);
    }

    public static List<SelectMenuChooseListener> getSelectMenuChooseListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getSelectMenuChooseListeners(messageId);
    }

    public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener var1);

    public List<SelectMenuChooseListener> getSelectMenuChooseListeners();

    public static ListenerManager<ButtonClickListener> addButtonClickListener(DiscordApi api, long messageId, ButtonClickListener listener) {
        return api.getUncachedMessageUtil().addButtonClickListener(messageId, listener);
    }

    public static List<ButtonClickListener> getButtonClickListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getButtonClickListeners(messageId);
    }

    public static List<ButtonClickListener> getButtonClickListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getButtonClickListeners(messageId);
    }

    public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener var1);

    public List<ButtonClickListener> getButtonClickListeners();

    public static ListenerManager<MessageEditListener> addMessageEditListener(DiscordApi api, long messageId, MessageEditListener listener) {
        return api.getUncachedMessageUtil().addMessageEditListener(messageId, listener);
    }

    public static List<MessageEditListener> getMessageEditListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageEditListeners(messageId);
    }

    public static List<MessageEditListener> getMessageEditListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageEditListeners(messageId);
    }

    public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener var1);

    public List<MessageEditListener> getMessageEditListeners();

    public static ListenerManager<ReactionRemoveListener> addReactionRemoveListener(DiscordApi api, long messageId, ReactionRemoveListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveListener(messageId, listener);
    }

    public static List<ReactionRemoveListener> getReactionRemoveListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveListeners(messageId);
    }

    public static List<ReactionRemoveListener> getReactionRemoveListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveListeners(messageId);
    }

    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener var1);

    public List<ReactionRemoveListener> getReactionRemoveListeners();

    public static ListenerManager<ReactionAddListener> addReactionAddListener(DiscordApi api, long messageId, ReactionAddListener listener) {
        return api.getUncachedMessageUtil().addReactionAddListener(messageId, listener);
    }

    public static List<ReactionAddListener> getReactionAddListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionAddListeners(messageId);
    }

    public static List<ReactionAddListener> getReactionAddListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionAddListeners(messageId);
    }

    public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener var1);

    public List<ReactionAddListener> getReactionAddListeners();

    public static ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(DiscordApi api, long messageId, ReactionRemoveAllListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveAllListener(messageId, listener);
    }

    public static List<ReactionRemoveAllListener> getReactionRemoveAllListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveAllListeners(messageId);
    }

    public static List<ReactionRemoveAllListener> getReactionRemoveAllListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveAllListeners(messageId);
    }

    public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener var1);

    public List<ReactionRemoveAllListener> getReactionRemoveAllListeners();

    public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener var1);

    public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners();

    public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener var1);

    public List<CachedMessagePinListener> getCachedMessagePinListeners();

    public static ListenerManager<MessageReplyListener> addMessageReplyListener(DiscordApi api, long messageId, MessageReplyListener listener) {
        return api.getUncachedMessageUtil().addMessageReplyListener(messageId, listener);
    }

    public static List<MessageReplyListener> getMessageReplyListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageReplyListeners(messageId);
    }

    public static List<MessageReplyListener> getMessageReplyListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageReplyListeners(messageId);
    }

    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener var1);

    public List<MessageReplyListener> getMessageReplyListeners();

    public static ListenerManager<MessageDeleteListener> addMessageDeleteListener(DiscordApi api, long messageId, MessageDeleteListener listener) {
        return api.getUncachedMessageUtil().addMessageDeleteListener(messageId, listener);
    }

    public static List<MessageDeleteListener> getMessageDeleteListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageDeleteListeners(messageId);
    }

    public static List<MessageDeleteListener> getMessageDeleteListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageDeleteListeners(messageId);
    }

    public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener var1);

    public List<MessageDeleteListener> getMessageDeleteListeners();

    public static <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(DiscordApi api, long messageId, T listener) {
        return api.getUncachedMessageUtil().addMessageAttachableListener(messageId, listener);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(DiscordApi api, String messageId, T listener) {
        return api.getUncachedMessageUtil().addMessageAttachableListener(messageId, listener);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(DiscordApi api, long messageId, T listener) {
        api.getUncachedMessageUtil().removeMessageAttachableListener(messageId, listener);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(DiscordApi api, String messageId, T listener) {
        api.getUncachedMessageUtil().removeMessageAttachableListener(messageId, listener);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageAttachableListeners(messageId);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageAttachableListeners(messageId);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(DiscordApi api, long messageId, Class<T> listenerClass, T listener) {
        api.getUncachedMessageUtil().removeListener(messageId, listenerClass, listener);
    }

    public static <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(DiscordApi api, String messageId, Class<T> listenerClass, T listener) {
        api.getUncachedMessageUtil().removeListener(messageId, listenerClass, listener);
    }

    public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(T var1);

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(T var1);

    public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners();

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

