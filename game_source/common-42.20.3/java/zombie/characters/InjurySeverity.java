/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

public enum InjurySeverity {
    LOW(0.5f),
    NORMAL(1.0f),
    HIGH(1.5f);

    public final float multiplier;

    private InjurySeverity(float multiplier) {
        this.multiplier = multiplier;
    }
}

