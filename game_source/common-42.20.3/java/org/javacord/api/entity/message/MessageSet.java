/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.message.Message;

public interface MessageSet
extends NavigableSet<Message> {
    default public Optional<Message> getOldestMessage() {
        return this.isEmpty() ? Optional.empty() : Optional.of((Message)this.first());
    }

    default public Optional<Message> getNewestMessage() {
        return this.isEmpty() ? Optional.empty() : Optional.of((Message)this.last());
    }

    default public CompletableFuture<Void> deleteAll() {
        return CompletableFuture.allOf((CompletableFuture[])this.stream().collect(Collectors.groupingBy(DiscordEntity::getApi, Collectors.toList())).entrySet().stream().map(entry -> Message.delete((DiscordApi)entry.getKey(), (Iterable)entry.getValue())).toArray(CompletableFuture[]::new));
    }

    public MessageSet subSet(Message var1, boolean var2, Message var3, boolean var4);

    public MessageSet subSet(Message var1, Message var2);

    public MessageSet headSet(Message var1, boolean var2);

    public MessageSet headSet(Message var1);

    public MessageSet tailSet(Message var1);

    public MessageSet tailSet(Message var1, boolean var2);
}

