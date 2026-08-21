/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component.internal;

import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.component.internal.ComponentBuilderDelegate;

public interface SelectMenuBuilderDelegate
extends ComponentBuilderDelegate {
    public ComponentType getType();

    public void setType(ComponentType var1);

    public void copy(SelectMenu var1);

    public void addChannelType(ChannelType var1);

    public void addOption(SelectMenuOption var1);

    public void removeOption(SelectMenuOption var1);

    public void setPlaceholder(String var1);

    public void setCustomId(String var1);

    public void setMinimumValues(int var1);

    public void setMaximumValues(int var1);

    public void setDisabled(boolean var1);

    public SelectMenu build();

    public void removeAllOptions();

    public String getCustomId();
}

