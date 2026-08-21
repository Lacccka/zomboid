/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.zip.PKWareExtraHeader;
import org.apache.commons.compress.archivers.zip.ZipShort;

public class X0019_EncryptionRecipientCertificateList
extends PKWareExtraHeader {
    static final ZipShort HEADER_ID = new ZipShort(25);

    public X0019_EncryptionRecipientCertificateList() {
        super(HEADER_ID);
    }
}

