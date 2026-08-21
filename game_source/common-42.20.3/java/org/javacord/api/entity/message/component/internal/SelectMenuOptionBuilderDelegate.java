/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component.internal;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.SelectMenuOption;

public interface SelectMenuOptionBuilderDelegate {
    public void copy(SelectMenuOption var1);

    public void setLabel(String var1);

    public void setValue(String var1);

    public void setDescription(String var1);

    public void setDefault(boolean var1);

    public void setEmoji(String var1);

    public void setEmoji(Emoji var1);

    public SelectMenuOption build();
}

