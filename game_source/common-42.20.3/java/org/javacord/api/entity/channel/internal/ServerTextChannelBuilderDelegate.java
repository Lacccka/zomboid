/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.internal.RegularServerChannelBuilderDelegate;

public interface ServerTextChannelBuilderDelegate
extends RegularServerChannelBuilderDelegate {
    public void setTopic(String var1);

    public void setCategory(ChannelCategory var1);

    public void setSlowmodeDelayInSeconds(int var1);

    public CompletableFuture<ServerTextChannel> create();
}

