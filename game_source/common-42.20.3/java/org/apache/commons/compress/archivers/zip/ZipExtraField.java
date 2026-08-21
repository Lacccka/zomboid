/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.ZipShort;

public interface ZipExtraField {
    public static final int EXTRAFIELD_HEADER_SIZE = 4;

    public byte[] getCentralDirectoryData();

    public ZipShort getCentralDirectoryLength();

    public ZipShort getHeaderId();

    public byte[] getLocalFileDataData();

    public ZipShort getLocalFileDataLength();

    public void parseFromCentralDirectoryData(byte[] var1, int var2, int var3) throws ZipException;

    public void parseFromLocalFileData(byte[] var1, int var2, int var3) throws ZipException;
}

