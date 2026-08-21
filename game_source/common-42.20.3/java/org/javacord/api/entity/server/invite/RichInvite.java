/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

import java.time.Instant;
import org.javacord.api.entity.server.invite.Invite;

public interface RichInvite
extends Invite {
    public int getUses();

    public int getMaxUses();

    public int getMaxAgeInSeconds();

    public boolean isTemporary();

    public Instant getCreationTimestamp();

    public boolean isRevoked();
}

