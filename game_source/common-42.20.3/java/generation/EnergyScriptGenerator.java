/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ScriptGenerator;
import generation.builders.EnergyBuilder;

public class EnergyScriptGenerator {
    protected static ScriptGenerator<EnergyBuilder> scriptFile(String name) {
        return EnergyScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<EnergyBuilder> scriptFile(String name, boolean append) {
        return new ScriptGenerator<EnergyBuilder>(name, append);
    }

    public static void main(String ... args2) {
        EnergyScriptGenerator.energies();
    }

    public static void energies() {
        EnergyScriptGenerator.scriptFile("energies").add(EnergyBuilder.withId("Electric").displayName("EC_Energy_Electric").color(0.63f, 0.78f, 0.6f).iconTexture("media/ui/Entity/Energy/icon_energy_electric.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_green.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_green.png")).add(EnergyBuilder.withId("Mechanical").displayName("EC_Energy_Mechanical").color(0.73f, 0.67f, 0.63f).iconTexture("media/ui/Entity/Energy/icon_energy_mechanical.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_yellowgreen.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_yellowgreen.png")).add(EnergyBuilder.withId("Thermal").displayName("EC_Energy_Thermal").color(0.83f, 0.72f, 0.52f).iconTexture("media/ui/Entity/Energy/icon_energy_thermal.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_orange.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_orange.png")).add(EnergyBuilder.withId("Steam").displayName("EC_Energy_Steam").color(0.87f, 0.64f, 0.62f).iconTexture("media/ui/Entity/Energy/icon_energy_steam.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_red.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_red.png")).add(EnergyBuilder.withId("VoidEnergy").displayName("EC_Energy_Void").color(0.0f, 0.0f, 0.0f).iconTexture("media/ui/Entity/Energy/icon_energy_solar.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_yellow.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_yellow.png")).add(EnergyBuilder.withId("Wind").displayName("EC_Energy_Wind").color(0.62f, 0.67f, 0.77f).iconTexture("media/ui/Entity/Energy/icon_energy_wind.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_blue.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_blue.png")).add(EnergyBuilder.withId("Solar").displayName("EC_Energy_Solar").color(0.84f, 0.81f, 0.54f).iconTexture("media/ui/Entity/Energy/icon_energy_solar.png").horizontalBarTexture("media/ui/Entity/Bars/bars_horz_yellow.png").verticalBarTexture("media/ui/Entity/Bars/bars_vert_yellow.png")).write();
    }
}

