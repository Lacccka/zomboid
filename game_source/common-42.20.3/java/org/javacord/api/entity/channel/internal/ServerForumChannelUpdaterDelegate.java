/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;

public interface ServerForumChannelUpdaterDelegate
extends RegularServerChannelUpdaterDelegate {
    public void setCategory(ChannelCategory var1);

    public void removeCategory();
}

