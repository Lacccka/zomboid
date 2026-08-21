/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.skinnedmodel.animation.debug;

import zombie.core.skinnedmodel.animation.debug.GenericNameValueRecordingFrame;
import zombie.core.skinnedmodel.animation.debug.NodeWeightSlot;
import zombie.util.list.PZArrayUtil;

public class GenericNameWeightRecordingFrame
extends GenericNameValueRecordingFrame {
    private NodeWeightSlot[] weights = new NodeWeightSlot[0];

    public GenericNameWeightRecordingFrame(String fileKey) {
        super(fileKey, "_weights");
    }

    @Override
    protected void onColumnAdded() {
        this.weights = PZArrayUtil.add(this.weights, new NodeWeightSlot());
    }

    public void logWeight(String name, int nodeId, int layer, float weight) {
        int columnIndex = this.getOrCreateColumn(name);
        this.getSlotAt(columnIndex).logWeight(nodeId, layer, weight);
    }

    public NodeWeightSlot getSlotAt(int i) {
        return this.weights[i];
    }

    public boolean isCellEmpty(int i) {
        return this.getSlotAt(i).isEmpty();
    }

    @Override
    public String getValueAt(int i) {
        return this.isCellEmpty(i) ? "" : this.getSlotAt(i).getCellString();
    }

    @Override
    public void reset() {
        int entryCount = this.getColumnCount();
        for (int i = 0; i < entryCount; ++i) {
            this.weights[i].reset();
        }
    }
}

