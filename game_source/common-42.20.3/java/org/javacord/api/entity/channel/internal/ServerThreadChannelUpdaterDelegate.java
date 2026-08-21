/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;

public interface ServerThreadChannelUpdaterDelegate
extends ServerChannelUpdaterDelegate {
    public void setArchivedFlag(boolean var1);

    public void setAutoArchiveDuration(AutoArchiveDuration var1);

    public void setLockedFlag(boolean var1);

    public void setInvitableFlag(boolean var1);

    public void setSlowmodeDelayInSeconds(int var1);
}

