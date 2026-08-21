/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiBoneWeight;
import jassimp.AiWrapperProvider;
import java.util.ArrayList;
import java.util.List;

public final class AiBone {
    private String m_name;
    private final List<AiBoneWeight> m_boneWeights = new ArrayList<AiBoneWeight>();
    private Object m_offsetMatrix;

    AiBone() {
    }

    public String getName() {
        return this.m_name;
    }

    public int getNumWeights() {
        return this.m_boneWeights.size();
    }

    public List<AiBoneWeight> getBoneWeights() {
        return this.m_boneWeights;
    }

    public <V3, M4, C, N, Q> M4 getOffsetMatrix(AiWrapperProvider<V3, M4, C, N, Q> aiWrapperProvider) {
        return (M4)this.m_offsetMatrix;
    }
}

