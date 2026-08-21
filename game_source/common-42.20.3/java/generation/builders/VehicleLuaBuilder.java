/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class VehicleLuaBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> create = this.property("create");
    private final Writeable.Property<String> init = this.property("init");
    private final Writeable.Property<String> checkOperate = this.property("checkOperate");
    private final Writeable.Property<String> update = this.property("update");
    private final Writeable.Property<String> use = this.property("use");
    private final Writeable.Property<String> checkEngine = this.property("checkEngine");

    public VehicleLuaBuilder create(String create) {
        this.create.setValue(create);
        return this;
    }

    public VehicleLuaBuilder init(String init) {
        this.init.setValue(init);
        return this;
    }

    public VehicleLuaBuilder checkOperate(String checkOperate) {
        this.checkOperate.setValue(checkOperate);
        return this;
    }

    public VehicleLuaBuilder update(String update) {
        this.update.setValue(update);
        return this;
    }

    public VehicleLuaBuilder use(String use) {
        this.use.setValue(use);
        return this;
    }

    public VehicleLuaBuilder checkEngine(String checkEngine) {
        this.checkEngine.setValue(checkEngine);
        return this;
    }
}

