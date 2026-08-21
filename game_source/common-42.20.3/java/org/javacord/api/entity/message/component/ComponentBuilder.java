/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.internal.ComponentBuilderDelegate;

public interface ComponentBuilder {
    public ComponentType getType();

    public ComponentBuilderDelegate getDelegate();
}

