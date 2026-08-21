/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.asn1;

import java.io.OutputStream;

public abstract class ASN1Generator {
    protected OutputStream _out;

    public ASN1Generator(OutputStream outputStream2) {
        this._out = outputStream2;
    }

    public abstract OutputStream getRawOutputStream();
}

