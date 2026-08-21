/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

public enum DamageModifier {
    NONE(0.0f),
    LOW(0.5f),
    STANDARD(1.0f),
    HIGH(2.0f),
    EXTREME(5.0f);

    public final float multiplier;

    private DamageModifier(float multiplier) {
        this.multiplier = multiplier;
    }
}

