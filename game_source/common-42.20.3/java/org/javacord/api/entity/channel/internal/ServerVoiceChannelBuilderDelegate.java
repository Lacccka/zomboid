/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.RegularServerChannelBuilderDelegate;

public interface ServerVoiceChannelBuilderDelegate
extends RegularServerChannelBuilderDelegate {
    public void setBitrate(int var1);

    public void setUserlimit(int var1);

    public void setCategory(ChannelCategory var1);

    public CompletableFuture<ServerVoiceChannel> create();
}

