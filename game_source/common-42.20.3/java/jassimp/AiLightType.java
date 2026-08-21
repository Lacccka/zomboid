/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

public enum AiLightType {
    DIRECTIONAL(1),
    POINT(2),
    SPOT(3),
    AMBIENT(4);

    private final int m_rawValue;

    static AiLightType fromRawValue(int n) {
        for (AiLightType aiLightType : AiLightType.values()) {
            if (aiLightType.m_rawValue != n) continue;
            return aiLightType;
        }
        throw new IllegalArgumentException("unexptected raw value: " + n);
    }

    private AiLightType(int n2) {
        this.m_rawValue = n2;
    }
}

