/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.common;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.OSFileStore;

@ThreadSafe
public abstract class AbstractOSFileStore
implements OSFileStore {
    private String name;
    private String volume;
    private String label;
    private String mount;
    private String options;
    private String uuid;
    private boolean local;

    protected AbstractOSFileStore(String name, String volume, String label, String mount, String options, String uuid, boolean local) {
        this.name = name;
        this.volume = volume;
        this.label = label;
        this.mount = mount;
        this.options = options;
        this.uuid = uuid;
        this.local = local;
    }

    protected AbstractOSFileStore() {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVolume() {
        return this.volume;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getMount() {
        return this.mount;
    }

    @Override
    public String getOptions() {
        return this.options;
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

    @Override
    public boolean isLocal() {
        return this.local;
    }

    public String toString() {
        return "OSFileStore [name=" + this.getName() + ", volume=" + this.getVolume() + ", label=" + this.getLabel() + ", logicalVolume=" + this.getLogicalVolume() + ", mount=" + this.getMount() + ", description=" + this.getDescription() + ", fsType=" + this.getType() + ", options=\"" + this.getOptions() + "\", uuid=" + this.getUUID() + ", isLocal=" + this.isLocal() + ", freeSpace=" + this.getFreeSpace() + ", usableSpace=" + this.getUsableSpace() + ", totalSpace=" + this.getTotalSpace() + ", freeInodes=" + this.getFreeInodes() + ", totalInodes=" + this.getTotalInodes() + "]";
    }
}

