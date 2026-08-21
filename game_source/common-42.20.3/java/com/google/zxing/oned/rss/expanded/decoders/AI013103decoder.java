/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.common.BitArray;
import com.google.zxing.oned.rss.expanded.decoders.AI013x0xDecoder;

final class AI013103decoder
extends AI013x0xDecoder {
    AI013103decoder(BitArray information) {
        super(information);
    }

    @Override
    protected void addWeightCode(StringBuilder buf, int weight) {
        buf.append("(3103)");
    }

    @Override
    protected int checkWeight(int weight) {
        return weight;
    }
}

