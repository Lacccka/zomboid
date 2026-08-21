/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.pqc.crypto.mceliece;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class McElieceCCA2KeyParameters
extends AsymmetricKeyParameter {
    private String params;

    public McElieceCCA2KeyParameters(boolean bl, String string) {
        super(bl);
        this.params = string;
    }

    public String getDigest() {
        return this.params;
    }
}

