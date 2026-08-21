/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import io.vavr.collection.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.user.UserChangeStatusEvent;
import org.javacord.core.event.user.OptionalUserEventImpl;

public class UserChangeStatusEventImpl
extends OptionalUserEventImpl
implements UserChangeStatusEvent {
    private final UserStatus newStatus;
    private final UserStatus oldStatus;
    private final Map<DiscordClient, UserStatus> newClientStatus;
    private final Map<DiscordClient, UserStatus> oldClientStatus;

    public UserChangeStatusEventImpl(DiscordApi api, long userId, UserStatus newStatus, UserStatus oldStatus, Map<DiscordClient, UserStatus> newClientStatus, Map<DiscordClient, UserStatus> oldClientStatus) {
        super(api, userId);
        this.newStatus = newStatus;
        this.oldStatus = oldStatus;
        this.newClientStatus = newClientStatus;
        this.oldClientStatus = oldClientStatus;
    }

    @Override
    public UserStatus getOldStatus() {
        return this.oldStatus;
    }

    @Override
    public UserStatus getNewStatus() {
        return this.newStatus;
    }

    @Override
    public UserStatus getOldStatusOnClient(DiscordClient client) {
        return this.oldClientStatus.getOrElse(client, UserStatus.OFFLINE);
    }

    @Override
    public UserStatus getNewStatusOnClient(DiscordClient client) {
        return this.newClientStatus.getOrElse(client, UserStatus.OFFLINE);
    }
}

