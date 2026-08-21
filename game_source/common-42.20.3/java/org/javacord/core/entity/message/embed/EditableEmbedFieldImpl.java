/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.core.entity.message.embed.EmbedFieldImpl;

public class EditableEmbedFieldImpl
implements EditableEmbedField {
    private EmbedFieldImpl delegate;

    public EditableEmbedFieldImpl(EmbedFieldImpl field) {
        this.delegate = field;
    }

    public void clearDelegate() {
        this.delegate = null;
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public String getValue() {
        return this.delegate.getValue();
    }

    @Override
    public boolean isInline() {
        return this.delegate.isInline();
    }

    @Override
    public void setName(String name) {
        this.delegate.setName(name);
    }

    @Override
    public void setValue(String value) {
        this.delegate.setValue(value);
    }

    @Override
    public void setInline(boolean inline) {
        this.delegate.setInline(inline);
    }
}

