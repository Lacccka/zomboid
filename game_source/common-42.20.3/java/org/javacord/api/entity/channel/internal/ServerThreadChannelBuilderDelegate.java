/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.internal.ServerChannelBuilderDelegate;

public interface ServerThreadChannelBuilderDelegate
extends ServerChannelBuilderDelegate {
    public void setInvitableFlag(Boolean var1);

    public void setChannelType(ChannelType var1);

    public void setAutoArchiveDuration(Integer var1);

    public void setSlowmodeDelayInSeconds(int var1);

    public CompletableFuture<ServerThreadChannel> create();
}

