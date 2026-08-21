/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiAnimBehavior {
    DEFAULT(0),
    CONSTANT(1),
    LINEAR(2),
    REPEAT(3);

    private final int m_rawValue;

    static AiAnimBehavior fromRawValue(int n) {
        for (AiAnimBehavior aiAnimBehavior : AiAnimBehavior.values()) {
            if (aiAnimBehavior.m_rawValue != n) continue;
            return aiAnimBehavior;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiAnimBehavior(int n2) {
        this.m_rawValue = n2;
    }
}

