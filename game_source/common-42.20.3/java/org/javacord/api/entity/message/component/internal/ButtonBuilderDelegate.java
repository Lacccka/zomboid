/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component.internal;

import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.internal.ComponentBuilderDelegate;

public interface ButtonBuilderDelegate
extends ComponentBuilderDelegate {
    public ComponentType getType();

    public void copy(Button var1);

    public void setEmoji(CustomEmoji var1);

    public void setEmoji(String var1);

    public void setEmoji(Emoji var1);

    public ButtonStyle getStyle();

    public String getLabel();

    public String getCustomId();

    public String getUrl();

    public Boolean isDisabled();

    public Emoji getEmoji();

    public void setStyle(ButtonStyle var1);

    public void setLabel(String var1);

    public void setCustomId(String var1);

    public void setUrl(String var1);

    public void setDisabled(Boolean var1);

    public Button build();
}

