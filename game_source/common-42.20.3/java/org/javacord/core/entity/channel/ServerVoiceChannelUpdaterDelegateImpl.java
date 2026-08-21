/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelUpdaterDelegate;
import org.javacord.core.entity.channel.RegularServerChannelUpdaterDelegateImpl;

public class ServerVoiceChannelUpdaterDelegateImpl
extends RegularServerChannelUpdaterDelegateImpl
implements ServerVoiceChannelUpdaterDelegate {
    protected Integer bitrate = null;
    protected Boolean nsfw = null;
    protected Integer userLimit = null;
    protected ChannelCategory category = null;
    protected boolean modifyCategory = false;

    public ServerVoiceChannelUpdaterDelegateImpl(ServerVoiceChannel channel) {
        super(channel);
    }

    @Override
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    @Override
    public void removeUserLimit() {
        this.userLimit = 0;
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
    public void setNsfw(Boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchChannel = super.prepareUpdateBody(body);
        if (this.bitrate != null) {
            body.put("bitrate", (int)this.bitrate);
            patchChannel = true;
        }
        if (this.nsfw != null) {
            body.put("nsfw", (boolean)this.nsfw);
            patchChannel = true;
        }
        if (this.userLimit != null) {
            body.put("user_limit", (int)this.userLimit);
            patchChannel = true;
        }
        if (this.modifyCategory) {
            body.put("parent_id", this.category == null ? null : this.category.getIdAsString());
            patchChannel = true;
        }
        return patchChannel;
    }
}

