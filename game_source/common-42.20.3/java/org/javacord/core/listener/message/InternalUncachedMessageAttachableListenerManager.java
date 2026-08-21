/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.message;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.UncachedMessageAttachableListenerManager;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalUncachedMessageAttachableListenerManager
extends UncachedMessageAttachableListenerManager {
    public DiscordApi getApi();

    @Override
    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(long messageId, MessageContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, MessageContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, MessageContextMenuCommandListener.class);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(String messageId) {
        try {
            return this.getMessageContextMenuCommandListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(long messageId, MessageComponentCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, MessageComponentCreateListener.class, listener);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, MessageComponentCreateListener.class);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners(String messageId) {
        try {
            return this.getMessageComponentCreateListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(long messageId, SelectMenuChooseListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, SelectMenuChooseListener.class, listener);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, SelectMenuChooseListener.class);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners(String messageId) {
        try {
            return this.getSelectMenuChooseListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<ButtonClickListener> addButtonClickListener(long messageId, ButtonClickListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, ButtonClickListener.class, listener);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, ButtonClickListener.class);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners(String messageId) {
        try {
            return this.getButtonClickListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<MessageEditListener> addMessageEditListener(long messageId, MessageEditListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, MessageEditListener.class, listener);
    }

    @Override
    default public List<MessageEditListener> getMessageEditListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, MessageEditListener.class);
    }

    @Override
    default public List<MessageEditListener> getMessageEditListeners(String messageId) {
        try {
            return this.getMessageEditListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(long messageId, ReactionRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, ReactionRemoveListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, ReactionRemoveListener.class);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners(String messageId) {
        try {
            return this.getReactionRemoveListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<ReactionAddListener> addReactionAddListener(long messageId, ReactionAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, ReactionAddListener.class, listener);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, ReactionAddListener.class);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners(String messageId) {
        try {
            return this.getReactionAddListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(long messageId, ReactionRemoveAllListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, ReactionRemoveAllListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, ReactionRemoveAllListener.class);
    }

    @Override
    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners(String messageId) {
        try {
            return this.getReactionRemoveAllListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(long messageId, MessageReplyListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, MessageReplyListener.class, listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, MessageReplyListener.class);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners(String messageId) {
        try {
            return this.getMessageReplyListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public ListenerManager<MessageDeleteListener> addMessageDeleteListener(long messageId, MessageDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, MessageDeleteListener.class, listener);
    }

    @Override
    default public List<MessageDeleteListener> getMessageDeleteListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId, MessageDeleteListener.class);
    }

    @Override
    default public List<MessageDeleteListener> getMessageDeleteListeners(String messageId) {
        try {
            return this.getMessageDeleteListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(long messageId, T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(MessageAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Message.class, messageId, listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(String messageId, T listener) {
        try {
            return this.addMessageAttachableListener(Long.parseLong(messageId), listener);
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(long messageId, T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(MessageAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> this.removeListener(messageId, (Class)listenerClass, listener));
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(String messageId, T listener) {
        try {
            this.removeMessageAttachableListener(Long.parseLong(messageId), listener);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(long messageId) {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Message.class, messageId);
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(String messageId) {
        try {
            return this.getMessageAttachableListeners(Long.parseLong(messageId));
        }
        catch (NumberFormatException ignored) {
            return Collections.emptyMap();
        }
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(long messageId, Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Message.class, messageId, listenerClass, listener);
    }

    @Override
    default public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(String messageId, Class<T> listenerClass, T listener) {
        try {
            this.removeListener(Long.parseLong(messageId), listenerClass, listener);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }
}

