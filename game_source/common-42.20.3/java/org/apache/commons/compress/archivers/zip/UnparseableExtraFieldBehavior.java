/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.ZipExtraField;

public interface UnparseableExtraFieldBehavior {
    public ZipExtraField onUnparseableExtraField(byte[] var1, int var2, int var3, boolean var4, int var5) throws ZipException;
}

