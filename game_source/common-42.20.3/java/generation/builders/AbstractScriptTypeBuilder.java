/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import zombie.scripting.ScriptType;

public abstract class AbstractScriptTypeBuilder
extends AbstractDynamicOrderPropertyBuilder {
    public AbstractScriptTypeBuilder(ScriptType type, String name) {
        this(type.getScriptTag(), name);
    }

    public AbstractScriptTypeBuilder(String type, String name) {
        super(name, (String k, String n) -> "%s %s".formatted(type, n));
    }

    public AbstractScriptTypeBuilder() {
    }
}

