/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.modes.gcm;

import java.util.Vector;
import org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;
import org.bouncycastle.util.Arrays;

public class Tables1kGCMExponentiator
implements GCMExponentiator {
    private Vector lookupPowX2;

    public void init(byte[] byArray) {
        long[] lArray = GCMUtil.asLongs(byArray);
        if (this.lookupPowX2 != null && Arrays.areEqual(lArray, (long[])this.lookupPowX2.elementAt(0))) {
            return;
        }
        this.lookupPowX2 = new Vector(8);
        this.lookupPowX2.addElement(lArray);
    }

    public void exponentiateX(long l, byte[] byArray) {
        long[] lArray = GCMUtil.oneAsLongs();
        int n = 0;
        while (l > 0L) {
            if ((l & 1L) != 0L) {
                this.ensureAvailable(n);
                GCMUtil.multiply(lArray, (long[])this.lookupPowX2.elementAt(n));
            }
            ++n;
            l >>>= 1;
        }
        GCMUtil.asBytes(lArray, byArray);
    }

    private void ensureAvailable(int n) {
        int n2 = this.lookupPowX2.size();
        if (n2 <= n) {
            long[] lArray = (long[])this.lookupPowX2.elementAt(n2 - 1);
            do {
                lArray = Arrays.clone(lArray);
                GCMUtil.square(lArray, lArray);
                this.lookupPowX2.addElement(lArray);
            } while (++n2 <= n);
        }
    }
}

