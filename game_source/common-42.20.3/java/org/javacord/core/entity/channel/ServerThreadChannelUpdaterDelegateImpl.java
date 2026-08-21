/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.internal.ServerThreadChannelUpdaterDelegate;
import org.javacord.core.entity.channel.ServerChannelUpdaterDelegateImpl;

public class ServerThreadChannelUpdaterDelegateImpl
extends ServerChannelUpdaterDelegateImpl
implements ServerThreadChannelUpdaterDelegate {
    private Boolean archived = null;
    private AutoArchiveDuration autoArchiveDuration = null;
    private Boolean locked = null;
    private Boolean invitable = null;
    private Integer delay = null;

    public ServerThreadChannelUpdaterDelegateImpl(ServerThreadChannel thread2) {
        super(thread2);
    }

    @Override
    public void setArchivedFlag(boolean archived) {
        this.archived = archived;
    }

    @Override
    public void setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration) {
        this.autoArchiveDuration = autoArchiveDuration;
    }

    @Override
    public void setLockedFlag(boolean locked) {
        this.locked = locked;
    }

    @Override
    public void setInvitableFlag(boolean invitable) {
        this.invitable = invitable;
    }

    @Override
    public void setSlowmodeDelayInSeconds(int delay) {
        this.delay = delay;
    }

    @Override
    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchThread = super.prepareUpdateBody(body);
        if (this.name != null) {
            body.put("name", this.name);
            patchThread = true;
        }
        if (this.archived != null) {
            body.put("archived", this.archived);
            patchThread = true;
        }
        if (this.autoArchiveDuration != null) {
            body.put("auto_archive_duration", this.autoArchiveDuration.asInt());
            patchThread = true;
        }
        if (this.locked != null) {
            body.put("locked", this.locked);
            patchThread = true;
        }
        if (this.invitable != null) {
            body.put("invitable", this.invitable);
            patchThread = true;
        }
        if (this.delay != null) {
            body.put("rate_limit_per_user", this.delay);
            patchThread = true;
        }
        return patchThread;
    }
}

