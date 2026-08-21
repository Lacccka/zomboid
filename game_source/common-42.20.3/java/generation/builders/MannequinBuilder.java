/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class MannequinBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<String> animSet = this.property("animSet");
    private final Writeable.Property<String> animState = this.property("animState");
    private final Writeable.Property<Boolean> female = this.property("female");
    private final Writeable.Property<String> model = this.property("model");
    private final Writeable.Property<String> outfit = this.property("outfit");
    private final Writeable.Property<String> pose = this.property("pose");
    private final Writeable.Property<String> texture = this.property("texture");

    public static MannequinBuilder withId(String id) {
        return new MannequinBuilder(id);
    }

    public MannequinBuilder(String name) {
        super(ScriptType.Mannequin, name);
    }

    public MannequinBuilder animSet(String animSet) {
        this.animSet.setValue(animSet);
        return this;
    }

    public MannequinBuilder animState(String animState) {
        this.animState.setValue(animState);
        return this;
    }

    public MannequinBuilder female(boolean female) {
        this.female.setValue(female);
        return this;
    }

    public MannequinBuilder model(String model) {
        this.model.setValue(model);
        return this;
    }

    public MannequinBuilder outfit(String outfit) {
        this.outfit.setValue(outfit);
        return this;
    }

    public MannequinBuilder pose(String pose) {
        this.pose.setValue(pose);
        return this;
    }

    public MannequinBuilder texture(String texture) {
        this.texture.setValue(texture);
        return this;
    }
}

