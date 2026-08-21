/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.ec;

import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;

public class SimpleLookupTable
implements ECLookupTable {
    private final ECPoint[] points;

    private static ECPoint[] copy(ECPoint[] eCPointArray, int n, int n2) {
        ECPoint[] eCPointArray2 = new ECPoint[n2];
        for (int i = 0; i < n2; ++i) {
            eCPointArray2[i] = eCPointArray[n + i];
        }
        return eCPointArray2;
    }

    public SimpleLookupTable(ECPoint[] eCPointArray, int n, int n2) {
        this.points = SimpleLookupTable.copy(eCPointArray, n, n2);
    }

    public int getSize() {
        return this.points.length;
    }

    public ECPoint lookup(int n) {
        return this.points[n];
    }
}

