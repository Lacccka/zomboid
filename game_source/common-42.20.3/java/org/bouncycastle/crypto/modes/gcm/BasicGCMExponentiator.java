/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.modes.gcm;

import org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;
import org.bouncycastle.util.Arrays;

public class BasicGCMExponentiator
implements GCMExponentiator {
    private long[] x;

    public void init(byte[] byArray) {
        this.x = GCMUtil.asLongs(byArray);
    }

    public void exponentiateX(long l, byte[] byArray) {
        long[] lArray = GCMUtil.oneAsLongs();
        if (l > 0L) {
            long[] lArray2 = Arrays.clone(this.x);
            do {
                if ((l & 1L) != 0L) {
                    GCMUtil.multiply(lArray, lArray2);
                }
                GCMUtil.square(lArray2, lArray2);
            } while ((l >>>= 1) > 0L);
        }
        GCMUtil.asBytes(lArray, byArray);
    }
}

