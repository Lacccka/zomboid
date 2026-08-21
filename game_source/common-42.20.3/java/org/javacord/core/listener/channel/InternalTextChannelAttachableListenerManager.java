/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
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
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.listener.channel.InternalChannelAttachableListenerManager;
import org.javacord.core.listener.channel.InternalServerThreadChannelAttachableListenerManager;
import org.javacord.core.util.ClassHelper;

public interface InternalTextChannelAttachableListenerManager
extends TextChannelAttachableListenerManager,
InternalServerThreadChannelAttachableListenerManager,
InternalChannelAttachableListenerManager {
    @Override
    public DiscordApi getApi();

    @Override
    public long getId();

    @Override
    default public ListenerManager<InteractionCreateListener> addInteractionCreateListener(InteractionCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), InteractionCreateListener.class, listener);
    }

    @Override
    default public List<InteractionCreateListener> getInteractionCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), InteractionCreateListener.class);
    }

    @Override
    default public ListenerManager<SlashCommandCreateListener> addSlashCommandCreateListener(SlashCommandCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), SlashCommandCreateListener.class, listener);
    }

    @Override
    default public List<SlashCommandCreateListener> getSlashCommandCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), SlashCommandCreateListener.class);
    }

    @Override
    default public ListenerManager<AutocompleteCreateListener> addAutocompleteCreateListener(AutocompleteCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), AutocompleteCreateListener.class, listener);
    }

    @Override
    default public List<AutocompleteCreateListener> getAutocompleteCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), AutocompleteCreateListener.class);
    }

    @Override
    default public ListenerManager<ModalSubmitListener> addModalSubmitListener(ModalSubmitListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ModalSubmitListener.class, listener);
    }

    @Override
    default public List<ModalSubmitListener> getModalSubmitListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ModalSubmitListener.class);
    }

    @Override
    default public ListenerManager<MessageContextMenuCommandListener> addMessageContextMenuCommandListener(MessageContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<MessageContextMenuCommandListener> getMessageContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<MessageComponentCreateListener> addMessageComponentCreateListener(MessageComponentCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageComponentCreateListener.class, listener);
    }

    @Override
    default public List<MessageComponentCreateListener> getMessageComponentCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageComponentCreateListener.class);
    }

    @Override
    default public ListenerManager<UserContextMenuCommandListener> addUserContextMenuCommandListener(UserContextMenuCommandListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), UserContextMenuCommandListener.class, listener);
    }

    @Override
    default public List<UserContextMenuCommandListener> getUserContextMenuCommandListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), UserContextMenuCommandListener.class);
    }

    @Override
    default public ListenerManager<SelectMenuChooseListener> addSelectMenuChooseListener(SelectMenuChooseListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), SelectMenuChooseListener.class, listener);
    }

    @Override
    default public List<SelectMenuChooseListener> getSelectMenuChooseListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), SelectMenuChooseListener.class);
    }

    @Override
    default public ListenerManager<ButtonClickListener> addButtonClickListener(ButtonClickListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ButtonClickListener.class, listener);
    }

    @Override
    default public List<ButtonClickListener> getButtonClickListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ButtonClickListener.class);
    }

    @Override
    default public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), UserStartTypingListener.class, listener);
    }

    @Override
    default public List<UserStartTypingListener> getUserStartTypingListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), UserStartTypingListener.class);
    }

    @Override
    default public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageEditListener.class, listener);
    }

    @Override
    default public List<MessageEditListener> getMessageEditListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageEditListener.class);
    }

    @Override
    default public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ChannelPinsUpdateListener.class, listener);
    }

    @Override
    default public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ChannelPinsUpdateListener.class);
    }

    @Override
    default public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ReactionRemoveListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ReactionRemoveListener.class);
    }

    @Override
    default public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ReactionAddListener.class, listener);
    }

    @Override
    default public List<ReactionAddListener> getReactionAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ReactionAddListener.class);
    }

    @Override
    default public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), ReactionRemoveAllListener.class, listener);
    }

    @Override
    default public List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), ReactionRemoveAllListener.class);
    }

    @Override
    default public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageCreateListener.class, listener);
    }

    @Override
    default public List<MessageCreateListener> getMessageCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageCreateListener.class);
    }

    @Override
    default public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), CachedMessageUnpinListener.class, listener);
    }

    @Override
    default public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), CachedMessageUnpinListener.class);
    }

    @Override
    default public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), CachedMessagePinListener.class, listener);
    }

    @Override
    default public List<CachedMessagePinListener> getCachedMessagePinListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), CachedMessagePinListener.class);
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageReplyListener.class, listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageReplyListener.class);
    }

    @Override
    default public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), MessageDeleteListener.class, listener);
    }

    @Override
    default public List<MessageDeleteListener> getMessageDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId(), MessageDeleteListener.class);
    }

    @Override
    default public <T extends TextChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends TextChannelAttachableListener>> addTextChannelAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(TextChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).flatMap(listenerClass -> {
            if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener))))).stream();
            }
            if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                return this.addChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener))))).stream();
            }
            return Stream.of(((DiscordApiImpl)this.getApi()).addObjectListener(TextChannel.class, this.getId(), listenerClass, listener));
        }).collect(Collectors.toList());
    }

    @Override
    default public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeTextChannelAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(TextChannelAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> {
            if (ServerThreadChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeServerThreadChannelAttachableListener((ServerThreadChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ServerThreadChannelAttachableListener)listener)))));
            } else if (ChannelAttachableListener.class.isAssignableFrom((Class<?>)listenerClass)) {
                this.removeChannelAttachableListener((ChannelAttachableListener)((Object)((ObjectAttachableListener)((Object)((ChannelAttachableListener)listener)))));
            } else {
                ((DiscordApiImpl)this.getApi()).removeObjectListener(TextChannel.class, this.getId(), listenerClass, listener);
            }
        });
    }

    @Override
    default public <T extends TextChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getTextChannelAttachableListeners() {
        Map listeners = ((DiscordApiImpl)this.getApi()).getObjectListeners(TextChannel.class, this.getId());
        this.getChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        this.getServerThreadChannelAttachableListeners().forEach((listener, listenerClasses) -> listeners.merge(listener, listenerClasses, (listenerClasses1, listenerClasses2) -> {
            listenerClasses1.addAll(listenerClasses2);
            return listenerClasses1;
        }));
        return listeners;
    }

    @Override
    default public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(TextChannel.class, this.getId(), listenerClass, listener);
    }
}

