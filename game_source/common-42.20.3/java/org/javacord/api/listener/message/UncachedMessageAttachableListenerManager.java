/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;

public interface UncachedMessageAttachableListenerManager {
    public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(long var1, MessageContextMenuCommandListener var3);

    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(long var1);

    public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners(String var1);

    public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(long var1, MessageComponentCreateListener var3);

    public List<MessageComponentCreateListener> getMessageComponentCreateListeners(long var1);

    public List<MessageComponentCreateListener> getMessageComponentCreateListeners(String var1);

    public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(long var1, SelectMenuChooseListener var3);

    public List<SelectMenuChooseListener> getSelectMenuChooseListeners(long var1);

    public List<SelectMenuChooseListener> getSelectMenuChooseListeners(String var1);

    public ListenerManager<ButtonClickListener> addButtonClickListener(long var1, ButtonClickListener var3);

    public List<ButtonClickListener> getButtonClickListeners(long var1);

    public List<ButtonClickListener> getButtonClickListeners(String var1);

    public ListenerManager<MessageEditListener> addMessageEditListener(long var1, MessageEditListener var3);

    public List<MessageEditListener> getMessageEditListeners(long var1);

    public List<MessageEditListener> getMessageEditListeners(String var1);

    public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(long var1, ReactionRemoveListener var3);

    public List<ReactionRemoveListener> getReactionRemoveListeners(long var1);

    public List<ReactionRemoveListener> getReactionRemoveListeners(String var1);

    public ListenerManager<ReactionAddListener> addReactionAddListener(long var1, ReactionAddListener var3);

    public List<ReactionAddListener> getReactionAddListeners(long var1);

    public List<ReactionAddListener> getReactionAddListeners(String var1);

    public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(long var1, ReactionRemoveAllListener var3);

    public List<ReactionRemoveAllListener> getReactionRemoveAllListeners(long var1);

    public List<ReactionRemoveAllListener> getReactionRemoveAllListeners(String var1);

    public ListenerManager<MessageReplyListener> addMessageReplyListener(long var1, MessageReplyListener var3);

    public List<MessageReplyListener> getMessageReplyListeners(long var1);

    public List<MessageReplyListener> getMessageReplyListeners(String var1);

    public ListenerManager<MessageDeleteListener> addMessageDeleteListener(long var1, MessageDeleteListener var3);

    public List<MessageDeleteListener> getMessageDeleteListeners(long var1);

    public List<MessageDeleteListener> getMessageDeleteListeners(String var1);

    public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(long var1, T var3);

    public <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addMessageAttachableListener(String var1, T var2);

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(long var1, T var3);

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(String var1, T var2);

    public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(long var1);

    public <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getMessageAttachableListeners(String var1);

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(long var1, Class<T> var3, T var4);

    public <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(String var1, Class<T> var2, T var3);
}

