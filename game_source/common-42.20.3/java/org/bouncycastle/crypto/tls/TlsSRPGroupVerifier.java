/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.params.SRP6GroupParameters;

public interface TlsSRPGroupVerifier {
    public boolean accept(SRP6GroupParameters var1);
}

