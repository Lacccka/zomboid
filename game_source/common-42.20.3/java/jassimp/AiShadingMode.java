/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiShadingMode {
    FLAT(1),
    GOURAUD(2),
    PHONG(3),
    BLINN(4),
    TOON(5),
    OREN_NAYAR(6),
    MINNAERT(7),
    COOK_TORRANCE(8),
    NO_SHADING(9),
    FRESNEL(10);

    private final int m_rawValue;

    static AiShadingMode fromRawValue(int n) {
        for (AiShadingMode aiShadingMode : AiShadingMode.values()) {
            if (aiShadingMode.m_rawValue != n) continue;
            return aiShadingMode;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiShadingMode(int n2) {
        this.m_rawValue = n2;
    }
}

