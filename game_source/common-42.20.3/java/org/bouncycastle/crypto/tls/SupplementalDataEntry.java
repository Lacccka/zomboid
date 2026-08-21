/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

public class SupplementalDataEntry {
    protected int dataType;
    protected byte[] data;

    public SupplementalDataEntry(int n, byte[] byArray) {
        this.dataType = n;
        this.data = byArray;
    }

    public int getDataType() {
        return this.dataType;
    }

    public byte[] getData() {
        return this.data;
    }
}

