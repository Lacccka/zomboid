/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class PhysicsHitReactionBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<Float> impulsePelvis = this.property("impulse_pelvis");
    private final Writeable.Property<Float> impulseSpine = this.property("impulse_spine");
    private final Writeable.Property<Float> impulseHead = this.property("impulse_head");
    private final Writeable.Property<Float> impulseLeftUpperLeg = this.property("impulse_left_upper_leg");
    private final Writeable.Property<Float> impulseLeftLowerLeg = this.property("impulse_left_lower_leg");
    private final Writeable.Property<Float> impulseRightUpperLeg = this.property("impulse_right_upper_leg");
    private final Writeable.Property<Float> impulseRightLowerLeg = this.property("impulse_right_lower_leg");
    private final Writeable.Property<Float> impulseLeftUpperArm = this.property("impulse_left_upper_arm");
    private final Writeable.Property<Float> impulseLeftLowerArm = this.property("impulse_left_lower_arm");
    private final Writeable.Property<Float> impulseRightUpperArm = this.property("impulse_right_upper_arm");
    private final Writeable.Property<Float> impulseRightLowerArm = this.property("impulse_right_lower_arm");
    private final Writeable.Property<Float> upwardImpulsePelvis = this.property("upwardImpulse_pelvis");
    private final Writeable.Property<Float> upwardImpulseSpine = this.property("upwardImpulse_spine");
    private final Writeable.Property<Float> upwardImpulseHead = this.property("upwardImpulse_head");
    private final Writeable.Property<Float> upwardImpulseLeftUpperLeg = this.property("upwardImpulse_left_upper_leg");
    private final Writeable.Property<Float> upwardImpulseLeftLowerLeg = this.property("upwardImpulse_left_lower_leg");
    private final Writeable.Property<Float> upwardImpulseRightUpperLeg = this.property("upwardImpulse_right_upper_leg");
    private final Writeable.Property<Float> upwardImpulseRightLowerLeg = this.property("upwardImpulse_right_lower_leg");
    private final Writeable.Property<Float> upwardImpulseLeftUpperArm = this.property("upwardImpulse_left_upper_arm");
    private final Writeable.Property<Float> upwardImpulseLeftLowerArm = this.property("upwardImpulse_left_lower_arm");
    private final Writeable.Property<Float> upwardImpulseRightUpperArm = this.property("upwardImpulse_right_upper_arm");
    private final Writeable.Property<Float> upwardImpulseRightLowerArm = this.property("upwardImpulse_right_lower_arm");

    public static PhysicsHitReactionBuilder withId(String id) {
        return new PhysicsHitReactionBuilder(id);
    }

    public PhysicsHitReactionBuilder(String name) {
        super(ScriptType.PhysicsHitReaction, name);
    }

    public PhysicsHitReactionBuilder impulsePelvis(float impulsePelvis) {
        this.impulsePelvis.setValue(Float.valueOf(impulsePelvis));
        return this;
    }

    public PhysicsHitReactionBuilder impulseSpine(float impulseSpine) {
        this.impulseSpine.setValue(Float.valueOf(impulseSpine));
        return this;
    }

    public PhysicsHitReactionBuilder impulseHead(float impulseHead) {
        this.impulseHead.setValue(Float.valueOf(impulseHead));
        return this;
    }

    public PhysicsHitReactionBuilder impulseLeftUpperLeg(float impulseLeftUpperLeg) {
        this.impulseLeftUpperLeg.setValue(Float.valueOf(impulseLeftUpperLeg));
        return this;
    }

    public PhysicsHitReactionBuilder impulseLeftLowerLeg(float impulseLeftLowerLeg) {
        this.impulseLeftLowerLeg.setValue(Float.valueOf(impulseLeftLowerLeg));
        return this;
    }

    public PhysicsHitReactionBuilder impulseRightUpperLeg(float impulseRightUpperLeg) {
        this.impulseRightUpperLeg.setValue(Float.valueOf(impulseRightUpperLeg));
        return this;
    }

    public PhysicsHitReactionBuilder impulseRightLowerLeg(float impulseRightLowerLeg) {
        this.impulseRightLowerLeg.setValue(Float.valueOf(impulseRightLowerLeg));
        return this;
    }

    public PhysicsHitReactionBuilder impulseLeftUpperArm(float impulseLeftUpperArm) {
        this.impulseLeftUpperArm.setValue(Float.valueOf(impulseLeftUpperArm));
        return this;
    }

    public PhysicsHitReactionBuilder impulseLeftLowerArm(float impulseLeftLowerArm) {
        this.impulseLeftLowerArm.setValue(Float.valueOf(impulseLeftLowerArm));
        return this;
    }

    public PhysicsHitReactionBuilder impulseRightUpperArm(float impulseRightUpperArm) {
        this.impulseRightUpperArm.setValue(Float.valueOf(impulseRightUpperArm));
        return this;
    }

    public PhysicsHitReactionBuilder impulseRightLowerArm(float impulseRightLowerArm) {
        this.impulseRightLowerArm.setValue(Float.valueOf(impulseRightLowerArm));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulsePelvis(float upwardImpulsePelvis) {
        this.upwardImpulsePelvis.setValue(Float.valueOf(upwardImpulsePelvis));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseSpine(float upwardImpulseSpine) {
        this.upwardImpulseSpine.setValue(Float.valueOf(upwardImpulseSpine));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseHead(float upwardImpulseHead) {
        this.upwardImpulseHead.setValue(Float.valueOf(upwardImpulseHead));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseLeftUpperLeg(float upwardImpulseLeftUpperLeg) {
        this.upwardImpulseLeftUpperLeg.setValue(Float.valueOf(upwardImpulseLeftUpperLeg));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseLeftLowerLeg(float upwardImpulseLeftLowerLeg) {
        this.upwardImpulseLeftLowerLeg.setValue(Float.valueOf(upwardImpulseLeftLowerLeg));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseRightUpperLeg(float upwardImpulseRightUpperLeg) {
        this.upwardImpulseRightUpperLeg.setValue(Float.valueOf(upwardImpulseRightUpperLeg));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseRightLowerLeg(float upwardImpulseRightLowerLeg) {
        this.upwardImpulseRightLowerLeg.setValue(Float.valueOf(upwardImpulseRightLowerLeg));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseLeftUpperArm(float upwardImpulseLeftUpperArm) {
        this.upwardImpulseLeftUpperArm.setValue(Float.valueOf(upwardImpulseLeftUpperArm));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseLeftLowerArm(float upwardImpulseLeftLowerArm) {
        this.upwardImpulseLeftLowerArm.setValue(Float.valueOf(upwardImpulseLeftLowerArm));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseRightUpperArm(float upwardImpulseRightUpperArm) {
        this.upwardImpulseRightUpperArm.setValue(Float.valueOf(upwardImpulseRightUpperArm));
        return this;
    }

    public PhysicsHitReactionBuilder upwardImpulseRightLowerArm(float upwardImpulseRightLowerArm) {
        this.upwardImpulseRightLowerArm.setValue(Float.valueOf(upwardImpulseRightLowerArm));
        return this;
    }
}

