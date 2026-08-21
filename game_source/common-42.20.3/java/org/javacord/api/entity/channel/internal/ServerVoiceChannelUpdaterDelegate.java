/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;

public interface ServerVoiceChannelUpdaterDelegate
extends RegularServerChannelUpdaterDelegate {
    public void setBitrate(int var1);

    public void setUserLimit(int var1);

    public void removeUserLimit();

    public void setCategory(ChannelCategory var1);

    public void removeCategory();

    public void setNsfw(Boolean var1);
}

