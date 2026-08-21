/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiTextureMapMode {
    WRAP(0),
    CLAMP(1),
    MIRROR(2),
    DECAL(3);

    private final int m_rawValue;

    static AiTextureMapMode fromRawValue(int n) {
        for (AiTextureMapMode aiTextureMapMode : AiTextureMapMode.values()) {
            if (aiTextureMapMode.m_rawValue != n) continue;
            return aiTextureMapMode;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiTextureMapMode(int n2) {
        this.m_rawValue = n2;
    }
}

