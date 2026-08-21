/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ScriptGenerator;
import generation.builders.SoundTimelineBuilder;

public class SoundTimelineScriptGenerator {
    protected static ScriptGenerator<SoundTimelineBuilder> scriptFile(String name) {
        return SoundTimelineScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<SoundTimelineBuilder> scriptFile(String name, boolean append) {
        return new ScriptGenerator<SoundTimelineBuilder>(name, append);
    }

    public static void main(String ... args2) {
        SoundTimelineScriptGenerator.sounds_object_fridge_soundTimeline();
        SoundTimelineScriptGenerator.sounds_vehicle_foley_soundTimeline();
    }

    public static void sounds_object_fridge_soundTimeline() {
        SoundTimelineScriptGenerator.scriptFile("sounds/objects/sounds_object_fridge_soundTimeline").add(SoundTimelineBuilder.withId("Object/Fridge/Running").electricityOn(3500)).write();
    }

    public static void sounds_vehicle_foley_soundTimeline() {
        SoundTimelineScriptGenerator.scriptFile("sounds/vehicles/sounds_vehicle_foley_soundTimeline").add(SoundTimelineBuilder.withId("Vehicle/Sport/Engine").idle(3500)).add(SoundTimelineBuilder.withId("Vehicle/Jeep/Engine").idle(3500)).add(SoundTimelineBuilder.withId("Vehicle/Standard/Engine").idle(5700)).add(SoundTimelineBuilder.withId("Vehicle/Van/Engine").idle(3600)).write();
    }
}

