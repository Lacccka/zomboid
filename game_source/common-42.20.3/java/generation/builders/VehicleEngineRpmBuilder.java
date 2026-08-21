/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.RpmDataBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class VehicleEngineRpmBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<RpmDataBuilder> data = this.listProperty("data", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Integer> version = this.property("VERSION");

    public static VehicleEngineRpmBuilder withId(String id) {
        return new VehicleEngineRpmBuilder(id);
    }

    public VehicleEngineRpmBuilder(String name) {
        super(ScriptType.VehicleEngineRPM, name);
    }

    public VehicleEngineRpmBuilder addData(RpmDataBuilder data) {
        this.data.addValues((RpmDataBuilder[])new RpmDataBuilder[]{data});
        return this;
    }

    public VehicleEngineRpmBuilder version(int version) {
        this.version.setValue(version);
        return this;
    }
}

