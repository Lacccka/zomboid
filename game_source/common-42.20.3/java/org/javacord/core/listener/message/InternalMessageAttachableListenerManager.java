/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.message;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.message.CachedMessagePinListener;
import org.javacord.api.listener.message.CachedMessageUnpinListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageAttachableListenerManager;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;

public interface InternalMessageAttachableListenerManager
extends MessageAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return MessageAttachableListenerManager.addMessageContextMenuCommandListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return MessageAttachableListenerManager.getMessageContextMenuCommandListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return MessageAttachableListenerManager.addMessageComponentCreateListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return MessageAttachableListenerManager.getMessageComponentCreateListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return MessageAttachableListenerManager.addSelectMenuChooseListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return MessageAttachableListenerManager.getSelectMenuChooseListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener listener) {
        return MessageAttachableListenerManager.addButtonClickListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners() {
        return MessageAttachableListenerManager.getButtonClickListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
        return MessageAttachableListenerManager.addMessageEditListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<MessageEditListener> getMessageEditListeners() {
        return MessageAttachableListenerManager.getMessageEditListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return MessageAttachableListenerManager.addReactionRemoveListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return MessageAttachableListenerManager.getReactionRemoveListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return MessageAttachableListenerManager.addReactionAddListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners() {
        return MessageAttachableListenerManager.getReactionAddListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
        return MessageAttachableListenerManager.addReactionRemoveAllListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
        return MessageAttachableListenerManager.getReactionRemoveAllListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, this.getId(), CachedMessageUnpinListener.class, listener);
    }

    @Override
    default public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, this.getId(), CachedMessageUnpinListener.class);
    }

    @Override
    default public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, this.getId(), CachedMessagePinListener.class, listener);
    }

    @Override
    default public List<CachedMessagePinListener> getCachedMessagePinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, this.getId(), CachedMessagePinListener.class);
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return MessageAttachableListenerManager.addMessageReplyListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners() {
        return MessageAttachableListenerManager.getMessageReplyListeners(this.getApi(), this.getId());
    }

    @Override
    default public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
        return MessageAttachableListenerManager.addMessageDeleteListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public List<MessageDeleteListener> getMessageDeleteListeners() {
        return MessageAttachableListenerManager.getMessageDeleteListeners(this.getApi(), this.getId());
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(T listener) {
        return MessageAttachableListenerManager.addMessageAttachableListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(T listener) {
        MessageAttachableListenerManager.removeMessageAttachableListener(this.getApi(), this.getId(), listener);
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners() {
        return MessageAttachableListenerManager.getMessageAttachableListeners(this.getApi(), this.getId());
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        MessageAttachableListenerManager.removeListener(this.getApi(), this.getId(), listenerClass, listener);
    }
}

