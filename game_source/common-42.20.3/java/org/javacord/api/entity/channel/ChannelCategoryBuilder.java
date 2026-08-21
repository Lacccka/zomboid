/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannelBuilder;
import org.javacord.api.entity.channel.internal.ChannelCategoryBuilderDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class ChannelCategoryBuilder
extends RegularServerChannelBuilder<ChannelCategoryBuilder> {
    private final ChannelCategoryBuilderDelegate delegate;

    public ChannelCategoryBuilder(Server server) {
        super(ChannelCategoryBuilder.class, DelegateFactory.createChannelCategoryBuilderDelegate(server));
        this.delegate = (ChannelCategoryBuilderDelegate)((RegularServerChannelBuilder)this).delegate;
    }

    public CompletableFuture<ChannelCategory> create() {
        return this.delegate.create();
    }
}

