/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import org.javacord.api.entity.Nameable;

public interface EmbedField
extends Nameable {
    public String getValue();

    public boolean isInline();
}

