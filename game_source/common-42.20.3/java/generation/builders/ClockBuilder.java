/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.ClockHandBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class ClockBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<Float> handOffset = this.listProperty("handOffset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Boolean> north = this.property("north");
    private final Writeable.Property<String> replacementSprite = this.property("replacementSprite");
    private final Writeable.ListProperty<ClockHandBuilder> hand = this.listProperty("hand", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public static ClockBuilder withId(String id) {
        return new ClockBuilder(id);
    }

    public ClockBuilder(String name) {
        super(ScriptType.Clock, name);
    }

    public ClockBuilder handOffset(float x, float y, float z) {
        this.handOffset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public ClockBuilder north(boolean north) {
        this.north.setValue(north);
        return this;
    }

    public ClockBuilder replacementSprite(String replacementSprite) {
        this.replacementSprite.setValue(replacementSprite);
        return this;
    }

    public ClockBuilder addHand(ClockHandBuilder hand) {
        this.hand.addValues((ClockHandBuilder[])new ClockHandBuilder[]{hand});
        return this;
    }
}

