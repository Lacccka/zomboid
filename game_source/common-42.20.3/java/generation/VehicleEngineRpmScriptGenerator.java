/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ScriptGenerator;
import generation.builders.RpmDataBuilder;
import generation.builders.VehicleEngineRpmBuilder;

public class VehicleEngineRpmScriptGenerator {
    protected static ScriptGenerator<VehicleEngineRpmBuilder> scriptFile(String name) {
        return VehicleEngineRpmScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<VehicleEngineRpmBuilder> scriptFile(String name, boolean append) {
        return new ScriptGenerator<VehicleEngineRpmBuilder>(name, append);
    }

    private static RpmDataBuilder data() {
        return new RpmDataBuilder();
    }

    public static void main(String ... args2) {
        VehicleEngineRpmScriptGenerator.vehicle_engine_rpm();
    }

    public static void vehicle_engine_rpm() {
        VehicleEngineRpmScriptGenerator.scriptFile("sounds/vehicles/vehicle_engine_rpm").add(VehicleEngineRpmBuilder.withId("generic").version(1).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3500).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4000).afterGearChange(2500)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4800).afterGearChange(2800)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(5300).afterGearChange(3000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(5800).afterGearChange(4500))).add(VehicleEngineRpmBuilder.withId("firebird").version(1).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3000).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3500).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4000).afterGearChange(2500)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4500).afterGearChange(2800)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(6000).afterGearChange(4500))).add(VehicleEngineRpmBuilder.withId("jeep").version(1).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3000).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3500).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4000).afterGearChange(2500)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4500).afterGearChange(2800)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(6000).afterGearChange(4500))).add(VehicleEngineRpmBuilder.withId("van").version(1).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3500).afterGearChange(2000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(3500).afterGearChange(2500)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4000).afterGearChange(2800)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(4500).afterGearChange(3000)).addData(VehicleEngineRpmScriptGenerator.data().gearChange(6000).afterGearChange(4500))).write();
    }
}

