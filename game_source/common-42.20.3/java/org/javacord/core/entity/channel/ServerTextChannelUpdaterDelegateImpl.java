/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.internal.ServerTextChannelUpdaterDelegate;
import org.javacord.core.entity.channel.RegularServerChannelUpdaterDelegateImpl;

public class ServerTextChannelUpdaterDelegateImpl
extends RegularServerChannelUpdaterDelegateImpl
implements ServerTextChannelUpdaterDelegate {
    protected String topic = null;
    protected Boolean nsfw = null;
    protected ChannelCategory category = null;
    protected boolean modifyCategory = false;
    protected int delay = 0;
    protected boolean modifyDelay = false;

    public ServerTextChannelUpdaterDelegateImpl(ServerTextChannel channel) {
        super(channel);
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setNsfwFlag(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public void setCategory(ChannelCategory category) {
        this.category = category;
        this.modifyCategory = true;
    }

    @Override
    public void removeCategory() {
        this.setCategory(null);
    }

    @Override
    public void setSlowmodeDelayInSeconds(int delay) {
        this.delay = delay;
        this.modifyDelay = true;
    }

    @Override
    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchChannel = super.prepareUpdateBody(body);
        if (this.topic != null) {
            body.put("topic", this.topic);
            patchChannel = true;
        }
        if (this.nsfw != null) {
            body.put("nsfw", (boolean)this.nsfw);
            patchChannel = true;
        }
        if (this.modifyCategory) {
            body.put("parent_id", this.category == null ? null : this.category.getIdAsString());
            patchChannel = true;
        }
        if (this.modifyDelay) {
            body.put("rate_limit_per_user", this.delay);
            patchChannel = true;
        }
        return patchChannel;
    }
}

