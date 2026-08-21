/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder;

import com.google.zxing.pdf417.PDF417Common;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final class BarcodeValue {
    private final Map<Integer, Integer> values = new HashMap<Integer, Integer>();

    BarcodeValue() {
    }

    void setValue(int value) {
        Integer confidence = this.values.get(value);
        if (confidence == null) {
            confidence = 0;
        }
        Integer n = confidence;
        Integer n2 = confidence = Integer.valueOf(confidence + 1);
        this.values.put(value, confidence);
    }

    int[] getValue() {
        int maxConfidence = -1;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> entry : this.values.entrySet()) {
            if (entry.getValue() > maxConfidence) {
                maxConfidence = entry.getValue();
                result.clear();
                result.add(entry.getKey());
                continue;
            }
            if (entry.getValue() != maxConfidence) continue;
            result.add(entry.getKey());
        }
        return PDF417Common.toIntArray(result);
    }

    public Integer getConfidence(int value) {
        return this.values.get(value);
    }
}

