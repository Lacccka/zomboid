/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.internal.ServerForumChannelUpdaterDelegate;
import org.javacord.core.entity.channel.RegularServerChannelUpdaterDelegateImpl;

public class ServerForumChannelUpdaterDelegateImpl
extends RegularServerChannelUpdaterDelegateImpl
implements ServerForumChannelUpdaterDelegate {
    protected ChannelCategory category = null;
    protected boolean modifyCategory = false;

    public ServerForumChannelUpdaterDelegateImpl(ServerForumChannel channel) {
        super(channel);
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
    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchChannel = super.prepareUpdateBody(body);
        if (this.modifyCategory) {
            body.put("parent_id", this.category == null ? null : this.category.getIdAsString());
            patchChannel = true;
        }
        return patchChannel;
    }
}

