/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.RegularServerChannelBuilderDelegate;

public interface ChannelCategoryBuilderDelegate
extends RegularServerChannelBuilderDelegate {
    public CompletableFuture<ChannelCategory> create();
}

