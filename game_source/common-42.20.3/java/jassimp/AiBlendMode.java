/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiBlendMode {
    DEFAULT(0),
    ADDITIVE(1);

    private final int m_rawValue;

    static AiBlendMode fromRawValue(int n) {
        for (AiBlendMode aiBlendMode : AiBlendMode.values()) {
            if (aiBlendMode.m_rawValue != n) continue;
            return aiBlendMode;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiBlendMode(int n2) {
        this.m_rawValue = n2;
    }
}

