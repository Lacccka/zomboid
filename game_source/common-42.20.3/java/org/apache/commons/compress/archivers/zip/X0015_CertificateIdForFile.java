/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.PKWareExtraHeader;
import org.apache.commons.compress.archivers.zip.ZipShort;

public class X0015_CertificateIdForFile
extends PKWareExtraHeader {
    static final ZipShort HEADER_ID = new ZipShort(21);
    private int rcount;
    private PKWareExtraHeader.HashAlgorithm hashAlg;

    public X0015_CertificateIdForFile() {
        super(HEADER_ID);
    }

    public PKWareExtraHeader.HashAlgorithm getHashAlgorithm() {
        return this.hashAlg;
    }

    public int getRecordCount() {
        return this.rcount;
    }

    @Override
    public void parseFromCentralDirectoryData(byte[] data, int offset, int length) throws ZipException {
        this.assertMinimalLength(4, length);
        super.parseFromCentralDirectoryData(data, offset, length);
        this.rcount = ZipShort.getValue(data, offset);
        this.hashAlg = PKWareExtraHeader.HashAlgorithm.getAlgorithmByCode(ZipShort.getValue(data, offset + 2));
    }
}

