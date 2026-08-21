/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os;

import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface OSFileStore {
    public String getName();

    public String getVolume();

    public String getLabel();

    public String getLogicalVolume();

    public String getMount();

    public String getDescription();

    public String getType();

    public String getOptions();

    public String getUUID();

    public boolean isLocal();

    public long getFreeSpace();

    public long getUsableSpace();

    public long getTotalSpace();

    public long getFreeInodes();

    public long getTotalInodes();

    public boolean updateAttributes();
}

