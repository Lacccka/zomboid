/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;

public interface ServerTextChannelUpdaterDelegate
extends RegularServerChannelUpdaterDelegate {
    public void setTopic(String var1);

    public void setNsfwFlag(boolean var1);

    public void setCategory(ChannelCategory var1);

    public void removeCategory();

    public void setSlowmodeDelayInSeconds(int var1);
}

