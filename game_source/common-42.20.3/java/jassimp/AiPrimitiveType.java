/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.util.Set;

public enum AiPrimitiveType {
    POINT(1),
    LINE(2),
    TRIANGLE(4),
    POLYGON(8);

    private final int m_rawValue;

    static void fromRawValue(Set<AiPrimitiveType> set, int n) {
        for (AiPrimitiveType aiPrimitiveType : AiPrimitiveType.values()) {
            if ((aiPrimitiveType.m_rawValue & n) == 0) continue;
            set.add(aiPrimitiveType);
        }
    }

    private AiPrimitiveType(int n2) {
        this.m_rawValue = n2;
    }
}

