/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.BodyDamage.Metabolics;
import zombie.characters.skills.PerkFactory;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.ActionSoundTime;
import zombie.scripting.objects.ModelKey;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.TimedActionKey;

public class TimedActionBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<String> actionAnim = this.property("actionAnim");
    private final Writeable.Property<String> animVarKey = this.property("animVarKey");
    private final Writeable.Property<String> animVarVal = this.property("animVarVal");
    private final Writeable.Property<SoundKey> completionSound = this.property("completionSound");
    private final Writeable.Property<Metabolics> metabolics = this.property("metabolics");
    private final Writeable.Property<Float> muscleStrainFactor = this.property("muscleStrainFactor");
    private final Writeable.ListProperty<BodyPartType> muscleStrainParts = this.listProperty("muscleStrainParts", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<PerkFactory.Perk> muscleStrainSkill = this.property("muscleStrainSkill");
    private final Writeable.Property<ModelKey> prop1;
    private final Writeable.Property<ModelKey> prop2;
    private final Writeable.Property<SoundKey> sound;
    private final Writeable.Property<ActionSoundTime> soundTime;
    private final Writeable.Property<Integer> time;

    public static TimedActionBuilder withId(TimedActionKey id) {
        return new TimedActionBuilder(id.id());
    }

    private TimedActionBuilder(String name) {
        super(ScriptType.TimedAction, name);
        String string = "Base.%s";
        this.prop1 = this.property("prop1", arg_0 -> TimedActionBuilder.lambda$prop1$0("Base.%s", arg_0));
        string = "Base.%s";
        this.prop2 = this.property("prop2", arg_0 -> TimedActionBuilder.lambda$prop2$0("Base.%s", arg_0));
        this.sound = this.property("sound");
        this.soundTime = this.property("soundTime");
        this.time = this.property("time");
    }

    public TimedActionBuilder actionAnim(String actionAnim) {
        this.actionAnim.setValue(actionAnim);
        return this;
    }

    public TimedActionBuilder animVarKey(String animVarKey) {
        this.animVarKey.setValue(animVarKey);
        return this;
    }

    public TimedActionBuilder animVarVal(String animVarVal) {
        this.animVarVal.setValue(animVarVal);
        return this;
    }

    public TimedActionBuilder completionSound(SoundKey completionSound) {
        this.completionSound.setValue(completionSound);
        return this;
    }

    public TimedActionBuilder metabolics(Metabolics metabolics) {
        this.metabolics.setValue(metabolics);
        return this;
    }

    public TimedActionBuilder muscleStrainFactor(float muscleStrainFactor) {
        this.muscleStrainFactor.setValue(Float.valueOf(muscleStrainFactor));
        return this;
    }

    public TimedActionBuilder muscleStrainParts(BodyPartType ... muscleStrainParts) {
        this.muscleStrainParts.addValues((BodyPartType[])muscleStrainParts);
        return this;
    }

    public TimedActionBuilder muscleStrainSkill(PerkFactory.Perk muscleStrainSkill) {
        this.muscleStrainSkill.setValue(muscleStrainSkill);
        return this;
    }

    public TimedActionBuilder prop1(ModelKey prop1) {
        this.prop1.setValue(prop1);
        return this;
    }

    public TimedActionBuilder prop2(ModelKey prop2) {
        this.prop2.setValue(prop2);
        return this;
    }

    public TimedActionBuilder sound(SoundKey sound) {
        this.sound.setValue(sound);
        return this;
    }

    public TimedActionBuilder soundTime(ActionSoundTime soundTime) {
        this.soundTime.setValue(soundTime);
        return this;
    }

    public TimedActionBuilder time(int time) {
        this.time.setValue(time);
        return this;
    }

    private static /* synthetic */ String lambda$prop2$0(String rec$, Object xva$0) {
        return "Base.%s".formatted(xva$0);
    }

    private static /* synthetic */ String lambda$prop1$0(String rec$, Object xva$0) {
        return "Base.%s".formatted(xva$0);
    }
}

