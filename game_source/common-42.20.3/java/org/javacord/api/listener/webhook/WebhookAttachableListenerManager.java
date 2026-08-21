/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.webhook;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageReplyListener;
import org.javacord.api.listener.webhook.WebhookAttachableListener;
import org.javacord.api.util.event.ListenerManager;

public interface WebhookAttachableListenerManager {
    public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener var1);

    public List<MessageCreateListener> getMessageCreateListeners();

    public ListenerManager<MessageReplyListener> addMessageReplyListener(MessageReplyListener var1);

    public List<MessageReplyListener> getMessageReplyListeners();

    public <T extends WebhookAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addWebhookAttachableListener(T var1);

    public <T extends WebhookAttachableListener & ObjectAttachableListener> void removeWebhookAttachableListener(T var1);

    public <T extends WebhookAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getWebhookAttachableListeners();

    public <T extends WebhookAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

