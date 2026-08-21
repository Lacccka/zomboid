/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerChannelChangePositionEvent
extends ServerChannelEvent {
    public int getNewPosition();

    public int getOldPosition();

    public int getNewRawPosition();

    public int getOldRawPosition();

    public Optional<ChannelCategory> getNewCategory();

    public Optional<ChannelCategory> getOldCategory();
}

