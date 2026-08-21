/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component.internal;

import java.util.Optional;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.component.internal.ComponentBuilderDelegate;

public interface TextInputBuilderDelegate
extends ComponentBuilderDelegate {
    public ComponentType getType();

    public void copy(TextInput var1);

    public TextInputStyle getStyle();

    public String getLabel();

    public String getCustomId();

    public Optional<String> getValue();

    public Optional<String> getPlaceholder();

    public Optional<Integer> getMinimumLength();

    public Optional<Integer> getMaximumLength();

    public boolean isRequired();

    public void setStyle(TextInputStyle var1);

    public void setLabel(String var1);

    public void setValue(String var1);

    public void setPlaceholder(String var1);

    public void setMinimumLength(Integer var1);

    public void setMaximumLength(Integer var1);

    public void setCustomId(String var1);

    public void setRequired(boolean var1);

    public TextInput build();
}

