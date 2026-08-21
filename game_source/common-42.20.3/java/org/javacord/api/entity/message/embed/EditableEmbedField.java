/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import org.javacord.api.entity.message.embed.EmbedField;

public interface EditableEmbedField
extends EmbedField {
    public void setName(String var1);

    public void setValue(String var1);

    public void setInline(boolean var1);
}

