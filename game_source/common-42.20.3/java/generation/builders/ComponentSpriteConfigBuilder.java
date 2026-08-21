/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.SpriteConfigFaceBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;
import zombie.scripting.objects.EntityKey;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.SoundKey;

public class ComponentSpriteConfigBuilder
extends AbstractDynamicOrderPropertyBuilder
implements ComponentBuilder {
    private final Writeable.Property<Integer> health = this.property("health");
    private final Writeable.Property<Integer> skillBaseHealth = this.property("skillBaseHealth");
    private final Writeable.Property<Integer> lightRadius = this.property("lightRadius");
    private final Writeable.Property<Integer> bonusHealth = this.property("bonusHealth");
    private final Writeable.Property<String> logicClass = this.property("LogicClass");
    private final Writeable.ListProperty<EntityKey> previousStage = this.listProperty("previousStage", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<PackTextureValidator> corner = this.property("corner");
    private final Writeable.Property<ItemKey> debugItem = this.property("debugItem");
    private final Writeable.Property<ItemKey> lightsourceFuel = this.property("lightsourceFuel");
    private final Writeable.Property<ItemTag> lightsourceItemTag;
    private final Writeable.Property<ItemKey> lightsourceItem;
    private final Writeable.Property<String> onCreate;
    private final Writeable.Property<String> timedActionOnIsValid;
    private final Writeable.Property<SoundKey> breakSound;
    private final Writeable.Property<String> onIsValid;
    private final Writeable.Property<Boolean> isPole;
    private final Writeable.Property<Boolean> isThumpable;
    private final Writeable.Property<Boolean> canBePadlocked;
    private final Writeable.Property<Boolean> needToBeAgainstWall;
    private final Writeable.Property<Boolean> needWindowFrame;
    private final Writeable.Property<Boolean> dontNeedFrame;
    private final Writeable.Property<Boolean> isProp;
    private final Writeable.ListProperty<SpriteConfigFaceBuilder> face;

    public ComponentSpriteConfigBuilder() {
        super("SpriteConfig");
        String string = "tags[%s]";
        this.lightsourceItemTag = this.property("lightsourceItem", arg_0 -> ComponentSpriteConfigBuilder.lambda$lightsourceItemTag$0("tags[%s]", arg_0));
        this.lightsourceItem = this.property("lightsourceItem");
        this.onCreate = this.property("OnCreate");
        this.timedActionOnIsValid = this.property("TimedActionOnIsValid");
        this.breakSound = this.property("BreakSound");
        this.onIsValid = this.property("OnIsValid");
        this.isPole = this.property("isPole");
        this.isThumpable = this.property("isThumpable");
        this.canBePadlocked = this.property("canBePadlocked");
        this.needToBeAgainstWall = this.property("needToBeAgainstWall");
        this.needWindowFrame = this.property("needWindowFrame");
        this.dontNeedFrame = this.property("dontNeedFrame");
        this.isProp = this.property("isProp");
        this.face = this.listProperty("face", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    }

    public ComponentSpriteConfigBuilder health(int health) {
        this.health.setValue(health);
        return this;
    }

    public ComponentSpriteConfigBuilder skillBaseHealth(int skillBaseHealth) {
        this.skillBaseHealth.setValue(skillBaseHealth);
        return this;
    }

    public ComponentSpriteConfigBuilder lightRadius(int lightRadius) {
        this.lightRadius.setValue(lightRadius);
        return this;
    }

    public ComponentSpriteConfigBuilder bonusHealth(int bonusHealth) {
        this.bonusHealth.setValue(bonusHealth);
        return this;
    }

    public ComponentSpriteConfigBuilder logicClass(String logicClass) {
        this.logicClass.setValue(logicClass);
        return this;
    }

    public ComponentSpriteConfigBuilder previousStage(EntityKey ... previousStage) {
        this.previousStage.addValues((EntityKey[])previousStage);
        return this;
    }

    public ComponentSpriteConfigBuilder corner(String corner) {
        this.corner.setValue(PackTextureValidator.of(corner));
        return this;
    }

    public ComponentSpriteConfigBuilder debugItem(ItemKey debugItem) {
        this.debugItem.setValue(debugItem);
        return this;
    }

    public ComponentSpriteConfigBuilder lightsourceFuel(ItemKey lightsourceFuel) {
        this.lightsourceFuel.setValue(lightsourceFuel);
        return this;
    }

    public ComponentSpriteConfigBuilder lightsourceItem(ItemTag lightsourceItemTag) {
        this.lightsourceItemTag.setValue(lightsourceItemTag);
        return this;
    }

    public ComponentSpriteConfigBuilder lightsourceItem(ItemKey lightsourceItem) {
        this.lightsourceItem.setValue(lightsourceItem);
        return this;
    }

    public ComponentSpriteConfigBuilder onCreate(String onCreate) {
        this.onCreate.setValue(onCreate);
        return this;
    }

    public ComponentSpriteConfigBuilder breakSound(SoundKey breakSound) {
        this.breakSound.setValue(breakSound);
        return this;
    }

    public ComponentSpriteConfigBuilder onIsValid(String onIsValid) {
        this.onIsValid.setValue(onIsValid);
        return this;
    }

    public ComponentSpriteConfigBuilder isPole(boolean isPole) {
        this.isPole.setValue(isPole);
        return this;
    }

    public ComponentSpriteConfigBuilder isThumpable(boolean isThumpable) {
        this.isThumpable.setValue(isThumpable);
        return this;
    }

    public ComponentSpriteConfigBuilder canBePadlocked(boolean canBePadlocked) {
        this.canBePadlocked.setValue(canBePadlocked);
        return this;
    }

    public ComponentSpriteConfigBuilder needToBeAgainstWall(boolean needToBeAgainstWall) {
        this.needToBeAgainstWall.setValue(needToBeAgainstWall);
        return this;
    }

    public ComponentSpriteConfigBuilder needWindowFrame(boolean needWindowFrame) {
        this.needWindowFrame.setValue(needWindowFrame);
        return this;
    }

    public ComponentSpriteConfigBuilder dontNeedFrame(boolean dontNeedFrame) {
        this.dontNeedFrame.setValue(dontNeedFrame);
        return this;
    }

    public ComponentSpriteConfigBuilder isProp(boolean isProp) {
        this.isProp.setValue(isProp);
        return this;
    }

    public ComponentSpriteConfigBuilder addFace(SpriteConfigFaceBuilder face) {
        this.face.addValues((SpriteConfigFaceBuilder[])new SpriteConfigFaceBuilder[]{face});
        return this;
    }

    public ComponentSpriteConfigBuilder timedActionOnIsValid(String timedActionOnIsValid) {
        this.timedActionOnIsValid.setValue(timedActionOnIsValid);
        return this;
    }

    private static /* synthetic */ String lambda$lightsourceItemTag$0(String rec$, Object xva$0) {
        return "tags[%s]".formatted(xva$0);
    }
}

