/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class PhysicsShapeBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<String> mesh = this.property("mesh");
    private final Writeable.ListProperty<Float> rotate = this.listProperty("rotate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> translate = this.listProperty("translate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public static PhysicsShapeBuilder withId(String id) {
        return new PhysicsShapeBuilder(id);
    }

    public PhysicsShapeBuilder(String name) {
        super(ScriptType.PhysicsShape, name);
    }

    public PhysicsShapeBuilder mesh(String model, String mesh) {
        return this.mesh("%s|%s".formatted(model, mesh));
    }

    public PhysicsShapeBuilder mesh(String mesh) {
        this.mesh.setValue(mesh);
        return this;
    }

    public PhysicsShapeBuilder rotate(float x, float y, float z) {
        this.rotate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public PhysicsShapeBuilder translate(float x, float y, float z) {
        this.translate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }
}

