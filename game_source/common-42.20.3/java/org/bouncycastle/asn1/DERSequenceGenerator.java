/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERGenerator;
import org.bouncycastle.asn1.DEROutputStream;

public class DERSequenceGenerator
extends DERGenerator {
    private final ByteArrayOutputStream _bOut = new ByteArrayOutputStream();

    public DERSequenceGenerator(OutputStream outputStream2) throws IOException {
        super(outputStream2);
    }

    public DERSequenceGenerator(OutputStream outputStream2, int n, boolean bl) throws IOException {
        super(outputStream2, n, bl);
    }

    public void addObject(ASN1Encodable aSN1Encodable) throws IOException {
        aSN1Encodable.toASN1Primitive().encode(new DEROutputStream(this._bOut));
    }

    public OutputStream getRawOutputStream() {
        return this._bOut;
    }

    public void close() throws IOException {
        this.writeDEREncoded(48, this._bOut.toByteArray());
    }
}

