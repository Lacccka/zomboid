/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import java.util.Set;

public enum AiSceneFlag {
    INCOMPLETE(1),
    VALIDATED(2),
    VALIDATION_WARNING(4),
    NON_VERBOSE_FORMAT(8),
    TERRAIN(16);

    private final int m_rawValue;

    static void fromRawValue(Set<AiSceneFlag> set, int n) {
        for (AiSceneFlag aiSceneFlag : AiSceneFlag.values()) {
            if ((aiSceneFlag.m_rawValue & n) == 0) continue;
            set.add(aiSceneFlag);
        }
    }

    private AiSceneFlag(int n2) {
        this.m_rawValue = n2;
    }
}

