/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.event.user.UserEvent;

public interface UserChangeDiscriminatorEvent
extends UserEvent {
    public String getNewDiscriminator();

    public String getOldDiscriminator();
}

