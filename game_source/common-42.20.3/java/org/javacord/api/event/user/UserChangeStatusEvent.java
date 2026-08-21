/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.user.OptionalUserEvent;

public interface UserChangeStatusEvent
extends OptionalUserEvent {
    public UserStatus getOldStatus();

    public UserStatus getNewStatus();

    default public UserStatus getOldDesktopStatus() {
        return this.getOldStatusOnClient(DiscordClient.DESKTOP);
    }

    default public UserStatus getOldMobileStatus() {
        return this.getOldStatusOnClient(DiscordClient.MOBILE);
    }

    default public UserStatus getOldWebStatus() {
        return this.getOldStatusOnClient(DiscordClient.WEB);
    }

    default public UserStatus getNewDesktopStatus() {
        return this.getNewStatusOnClient(DiscordClient.DESKTOP);
    }

    default public UserStatus getNewMobileStatus() {
        return this.getNewStatusOnClient(DiscordClient.MOBILE);
    }

    default public UserStatus getNewWebStatus() {
        return this.getNewStatusOnClient(DiscordClient.WEB);
    }

    public UserStatus getOldStatusOnClient(DiscordClient var1);

    public UserStatus getNewStatusOnClient(DiscordClient var1);

    default public boolean hasStatusChangeOnClient(DiscordClient client) {
        return this.getOldStatusOnClient(client) != this.getNewStatusOnClient(client);
    }
}

