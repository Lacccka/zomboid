/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.AnimationScriptGenerator;
import generation.AnimationsMeshScriptGenerator;
import generation.CharacterProfessionScriptGenerator;
import generation.CharacterTraitScriptGenerator;
import generation.ClockScriptGenerator;
import generation.CraftRecipeScriptGenerator;
import generation.EnergyScriptGenerator;
import generation.EntityScriptGenerator;
import generation.EvolvedrecipeScriptGenerator;
import generation.FixingScriptGenerator;
import generation.FluidScriptGenerator;
import generation.ItemScriptGenerator;
import generation.MannequinScriptGenerator;
import generation.ModelScriptGenerator;
import generation.PhysicsHitReactionScriptGenerator;
import generation.PhysicsShapeScriptGenerator;
import generation.SoundScriptGenerator;
import generation.SoundTimelineScriptGenerator;
import generation.TimedActionScriptGenerator;
import generation.VehicleEngineRpmScriptGenerator;
import generation.VehicleScriptGenerator;
import generation.VehicleTemplateScriptGenerator;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class ScriptFileGenerator {
    public static void main(String[] args2) throws Exception {
        ScriptFileGenerator.deleteFolder(Paths.get("media/scripts/generated", new String[0]));
        AnimationScriptGenerator.main(new String[0]);
        AnimationsMeshScriptGenerator.main(new String[0]);
        ClockScriptGenerator.main(new String[0]);
        CraftRecipeScriptGenerator.main(new String[0]);
        EnergyScriptGenerator.main(new String[0]);
        EntityScriptGenerator.main(new String[0]);
        EvolvedrecipeScriptGenerator.main(new String[0]);
        FixingScriptGenerator.main(new String[0]);
        FluidScriptGenerator.main(new String[0]);
        ItemScriptGenerator.main(new String[0]);
        MannequinScriptGenerator.main(new String[0]);
        ModelScriptGenerator.main(new String[0]);
        PhysicsHitReactionScriptGenerator.main(new String[0]);
        PhysicsShapeScriptGenerator.main(new String[0]);
        SoundScriptGenerator.main(new String[0]);
        SoundTimelineScriptGenerator.main(new String[0]);
        TimedActionScriptGenerator.main(new String[0]);
        VehicleEngineRpmScriptGenerator.main(new String[0]);
        VehicleScriptGenerator.main(new String[0]);
        VehicleTemplateScriptGenerator.main(new String[0]);
        CharacterTraitScriptGenerator.main(new String[0]);
        CharacterProfessionScriptGenerator.main(new String[0]);
    }

    public static void deleteFolder(Path target) throws IOException {
        if (Files.exists(target, new LinkOption[0])) {
            for (Path path : Files.walk(target, new FileVisitOption[0]).sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}

