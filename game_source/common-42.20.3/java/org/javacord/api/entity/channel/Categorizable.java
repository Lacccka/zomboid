/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.ChannelSpecialization;

public interface Categorizable
extends ChannelSpecialization {
    public Optional<ChannelCategory> getCategory();

    public CompletableFuture<Void> updateCategory(ChannelCategory var1);

    public CompletableFuture<Void> removeCategory();
}

