/*
 * Decompiled with CFR 0.152.
 */
package generation;

import generation.ScriptGenerator;
import generation.builders.ClockBuilder;
import generation.builders.ClockHandBuilder;

public class ClockScriptGenerator {
    protected static ScriptGenerator<ClockBuilder> scriptFile(String name) {
        return ClockScriptGenerator.scriptFile(name, false);
    }

    protected static ScriptGenerator<ClockBuilder> scriptFile(String name, boolean append) {
        return new ScriptGenerator<ClockBuilder>(name, append);
    }

    public static ClockHandBuilder hand(String name) {
        return new ClockHandBuilder(name);
    }

    public static void main(String ... args2) {
        ClockScriptGenerator.clocks();
    }

    public static void clocks() {
        ClockScriptGenerator.scriptFile("clocks").add(ClockBuilder.withId("location_community_school_01_32").replacementSprite("animated_clock_01_0").north(true).handOffset(0.5f, 0.175f, 0.775f).addHand(ClockScriptGenerator.hand("hour").length(0.14f).thickness(0.019f).texture("IsoObject/ClockHandHourRed").rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.18f).thickness(0.02f).texture("IsoObject/ClockHandMinuteBlack").rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("location_community_school_01_33").replacementSprite("animated_clock_01_1").north(false).handOffset(0.17f, 0.5f, 0.778f).addHand(ClockScriptGenerator.hand("hour").length(0.14f).thickness(0.019f).texture("IsoObject/ClockHandHourRed").rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.18f).thickness(0.02f).texture("IsoObject/ClockHandMinuteBlack").rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("walls_decoration_01_105").replacementSprite("animated_clock_01_3").north(true).handOffset(0.5f, 0.155f, 0.83f).addHand(ClockScriptGenerator.hand("hour").length(0.112f).thickness(0.0152f).texture("IsoObject/ClockHandHourRed").rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.144f).thickness(0.016f).texture("IsoObject/ClockHandMinuteBlack").rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("walls_decoration_01_104").replacementSprite("animated_clock_01_2").north(false).handOffset(0.15f, 0.5f, 0.833f).addHand(ClockScriptGenerator.hand("hour").length(0.112f).thickness(0.0152f).texture("IsoObject/ClockHandHourRed").rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.144f).thickness(0.016f).texture("IsoObject/ClockHandMinuteBlack").rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("walls_detailing_02_56").replacementSprite("animated_clock_01_4").north(true).handOffset(1.365f, 0.42f, 0.52f).addHand(ClockScriptGenerator.hand("hour").length(0.42f).thickness(0.0f).texture("IsoObject/ClockHand2").textureInfo(53, 454, 26, 357).rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.565f).thickness(0.0f).texture("IsoObject/ClockHand2").textureInfo(53, 454, 26, 357).rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("walls_detailing_02_57").replacementSprite("animated_clock_01_5")).add(ClockBuilder.withId("walls_detailing_02_58").replacementSprite("animated_clock_01_6").north(false).handOffset(0.35f, 0.31f, 0.48f).addHand(ClockScriptGenerator.hand("hour").length(0.42f).thickness(0.0f).texture("IsoObject/ClockHand2").textureInfo(53, 454, 26, 357).rgba(1.0f, 1.0f, 1.0f, 1.0f)).addHand(ClockScriptGenerator.hand("minute").length(0.565f).thickness(0.0f).texture("IsoObject/ClockHand2").textureInfo(53, 454, 26, 357).rgba(1.0f, 1.0f, 1.0f, 1.0f))).add(ClockBuilder.withId("walls_detailing_02_59").replacementSprite("animated_clock_01_7")).write();
    }
}

