/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ScriptGenerator;
import generation.builders.AnimationBuilder;
import generation.builders.CopyFrameBuilder;
import generation.builders.CopyFramesBuilder;

public class AnimationScriptGenerator {
    protected static ScriptGenerator<AnimationBuilder> scriptFile(String name) {
        return AnimationScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<AnimationBuilder> scriptFile(String name, boolean append) {
        return new ScriptGenerator<AnimationBuilder>(name, append);
    }

    public static CopyFrameBuilder copyFrame() {
        return new CopyFrameBuilder();
    }

    public static CopyFramesBuilder copyFrames() {
        return new CopyFramesBuilder();
    }

    public static void main(String ... args2) {
        AnimationScriptGenerator.animations();
    }

    public static void animations() {
        AnimationScriptGenerator.scriptFile("animations").add(AnimationBuilder.withId("Runtime1").addCopyFrame(AnimationScriptGenerator.copyFrame().frame(1).source("Bob_IdleDrinkPopCan").sourceFrame(1)).addCopyFrame(AnimationScriptGenerator.copyFrame().frame(21).source("Bob_IdleDrinkPopCan").sourceFrame(21))).add(AnimationBuilder.withId("Runtime2").addCopyFrames(AnimationScriptGenerator.copyFrames().frame(1).source("Bob_IdleDrinkPopCan").sourceFrame1(1).sourceFrame2(21))).write();
    }
}

