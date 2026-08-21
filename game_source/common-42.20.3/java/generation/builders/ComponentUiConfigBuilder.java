/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;

public class ComponentUiConfigBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.Property<String> xuiSkin = this.property("xuiSkin");
    private final Writeable.Property<String> entityStyle = this.property("entityStyle");
    private final Writeable.Property<Boolean> uiEnabled = this.property("uiEnabled");

    public ComponentUiConfigBuilder() {
        super("UiConfig");
        this.xuiSkin("default");
    }

    public ComponentUiConfigBuilder xuiSkin(String xuiSkin) {
        this.xuiSkin.setValue(xuiSkin);
        return this;
    }

    public ComponentUiConfigBuilder entityStyle(String entityStyle) {
        this.entityStyle.setValue(entityStyle);
        return this;
    }

    public ComponentUiConfigBuilder uiEnabled(boolean uiEnabled) {
        this.uiEnabled.setValue(uiEnabled);
        return this;
    }
}

