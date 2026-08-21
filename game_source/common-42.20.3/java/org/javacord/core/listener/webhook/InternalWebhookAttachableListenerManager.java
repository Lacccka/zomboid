/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.webhook;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.webhook.WebhookAttachableListener;
import org.javacord.api.listener.webhook.WebhookAttachableListenerManager;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalWebhookAttachableListenerManager
extends WebhookAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Webhook.class, this.getId(), MessageCreateListener.class, listener);
    }

    @Override
    default public List<MessageCreateListener> getMessageCreateListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Webhook.class, this.getId(), MessageCreateListener.class);
    }

    @Override
    default public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Webhook.class, this.getId(), MessageReplyListener.class, listener);
    }

    @Override
    default public List<MessageReplyListener> getMessageReplyListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Webhook.class, this.getId(), MessageReplyListener.class);
    }

    @Override
    default public <T extends WebhookAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addWebhookAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(WebhookAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Webhook.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends WebhookAttachableListener & ObjectAttachableListener> void removeWebhookAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(WebhookAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(Webhook.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends WebhookAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getWebhookAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Webhook.class, this.getId());
    }

    @Override
    default public <T extends WebhookAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Webhook.class, this.getId(), listenerClass, listener);
    }
}

