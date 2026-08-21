/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelChangePositionEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelChangePositionEventImpl
extends ServerChannelEventImpl
implements ServerChannelChangePositionEvent {
    private final int newPosition;
    private final int oldPosition;
    private final int newRawPosition;
    private final int oldRawPosition;
    private final ChannelCategory newCategory;
    private final ChannelCategory oldCategory;

    public ServerChannelChangePositionEventImpl(ServerChannel channel, int newPosition, int oldPosition, int newRawPosition, int oldRawPosition, ChannelCategory newCategory, ChannelCategory oldCategory) {
        super(channel);
        this.newPosition = newPosition;
        this.oldPosition = oldPosition;
        this.newRawPosition = newRawPosition;
        this.oldRawPosition = oldRawPosition;
        this.newCategory = newCategory;
        this.oldCategory = oldCategory;
    }

    @Override
    public int getNewPosition() {
        return this.newPosition;
    }

    @Override
    public int getOldPosition() {
        return this.oldPosition;
    }

    @Override
    public int getNewRawPosition() {
        return this.newRawPosition;
    }

    @Override
    public int getOldRawPosition() {
        return this.oldRawPosition;
    }

    @Override
    public Optional<ChannelCategory> getNewCategory() {
        return Optional.ofNullable(this.newCategory);
    }

    @Override
    public Optional<ChannelCategory> getOldCategory() {
        return Optional.ofNullable(this.oldCategory);
    }
}

