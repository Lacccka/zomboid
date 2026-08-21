/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.Named;
import generation.builders.Writeable;

public interface ComponentBuilder
extends Writeable,
Named {
    default public String getType() {
        return this.getName();
    }
}

