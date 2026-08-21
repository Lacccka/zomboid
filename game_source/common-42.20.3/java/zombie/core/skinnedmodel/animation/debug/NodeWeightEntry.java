/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.skinnedmodel.animation.debug;

public class NodeWeightEntry {
    public float weight;
    public int nodeId;
    public int layerIdx;

    public boolean isEmpty() {
        return this.weight < 1.0E-5f;
    }

    public void reset() {
        this.weight = 0.0f;
        this.layerIdx = 0;
    }
}

