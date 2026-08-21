/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiMeshAnim;
import jassimp.AiNodeAnim;
import java.util.ArrayList;
import java.util.List;

public final class AiAnimation {
    private final String m_name;
    private final double m_duration;
    private final double m_ticksPerSecond;
    private final List<AiNodeAnim> m_nodeAnims = new ArrayList<AiNodeAnim>();

    AiAnimation(String string, double d, double d2) {
        this.m_name = string;
        this.m_duration = d;
        this.m_ticksPerSecond = d2;
    }

    public String getName() {
        return this.m_name;
    }

    public double getDuration() {
        return this.m_duration;
    }

    public double getTicksPerSecond() {
        return this.m_ticksPerSecond;
    }

    public int getNumChannels() {
        return this.m_nodeAnims.size();
    }

    public List<AiNodeAnim> getChannels() {
        return this.m_nodeAnims;
    }

    public int getNumMeshChannels() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public List<AiMeshAnim> getMeshChannels() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}

