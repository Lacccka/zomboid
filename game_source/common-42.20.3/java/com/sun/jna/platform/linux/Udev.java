/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;

public interface Udev
extends Library {
    public static final Udev INSTANCE = Native.load("udev", Udev.class);

    public UdevContext udev_new();

    public UdevContext udev_ref(UdevContext var1);

    public UdevContext udev_unref(UdevContext var1);

    public UdevDevice udev_device_new_from_syspath(UdevContext var1, String var2);

    public UdevEnumerate udev_enumerate_new(UdevContext var1);

    public UdevEnumerate udev_enumerate_ref(UdevEnumerate var1);

    public UdevEnumerate udev_enumerate_unref(UdevEnumerate var1);

    public int udev_enumerate_add_match_subsystem(UdevEnumerate var1, String var2);

    public int udev_enumerate_scan_devices(UdevEnumerate var1);

    public UdevListEntry udev_enumerate_get_list_entry(UdevEnumerate var1);

    public UdevListEntry udev_list_entry_get_next(UdevListEntry var1);

    public String udev_list_entry_get_name(UdevListEntry var1);

    public UdevDevice udev_device_ref(UdevDevice var1);

    public UdevDevice udev_device_unref(UdevDevice var1);

    public UdevDevice udev_device_get_parent(UdevDevice var1);

    public UdevDevice udev_device_get_parent_with_subsystem_devtype(UdevDevice var1, String var2, String var3);

    public String udev_device_get_syspath(UdevDevice var1);

    public String udev_device_get_sysname(UdevDevice var1);

    public String udev_device_get_devnode(UdevDevice var1);

    public String udev_device_get_devtype(UdevDevice var1);

    public String udev_device_get_subsystem(UdevDevice var1);

    public String udev_device_get_sysattr_value(UdevDevice var1, String var2);

    public String udev_device_get_property_value(UdevDevice var1, String var2);

    public static class UdevDevice
    extends PointerType {
        public UdevDevice ref() {
            return INSTANCE.udev_device_ref(this);
        }

        public void unref() {
            INSTANCE.udev_device_unref(this);
        }

        public UdevDevice getParent() {
            return INSTANCE.udev_device_get_parent(this);
        }

        public UdevDevice getParentWithSubsystemDevtype(String subsystem, String devtype) {
            return INSTANCE.udev_device_get_parent_with_subsystem_devtype(this, subsystem, devtype);
        }

        public String getSyspath() {
            return INSTANCE.udev_device_get_syspath(this);
        }

        public String getSysname() {
            return INSTANCE.udev_device_get_syspath(this);
        }

        public String getDevnode() {
            return INSTANCE.udev_device_get_devnode(this);
        }

        public String getDevtype() {
            return INSTANCE.udev_device_get_devtype(this);
        }

        public String getSubsystem() {
            return INSTANCE.udev_device_get_subsystem(this);
        }

        public String getSysattrValue(String sysattr) {
            return INSTANCE.udev_device_get_sysattr_value(this, sysattr);
        }

        public String getPropertyValue(String key) {
            return INSTANCE.udev_device_get_property_value(this, key);
        }
    }

    public static class UdevListEntry
    extends PointerType {
        public UdevListEntry getNext() {
            return INSTANCE.udev_list_entry_get_next(this);
        }

        public String getName() {
            return INSTANCE.udev_list_entry_get_name(this);
        }
    }

    public static class UdevEnumerate
    extends PointerType {
        public UdevEnumerate ref() {
            return INSTANCE.udev_enumerate_ref(this);
        }

        public void unref() {
            INSTANCE.udev_enumerate_unref(this);
        }

        public int addMatchSubsystem(String subsystem) {
            return INSTANCE.udev_enumerate_add_match_subsystem(this, subsystem);
        }

        public int scanDevices() {
            return INSTANCE.udev_enumerate_scan_devices(this);
        }

        public UdevListEntry getListEntry() {
            return INSTANCE.udev_enumerate_get_list_entry(this);
        }
    }

    public static class UdevContext
    extends PointerType {
        public UdevContext ref() {
            return INSTANCE.udev_ref(this);
        }

        public void unref() {
            INSTANCE.udev_unref(this);
        }

        public UdevEnumerate enumerateNew() {
            return INSTANCE.udev_enumerate_new(this);
        }

        public UdevDevice deviceNewFromSyspath(String syspath) {
            return INSTANCE.udev_device_new_from_syspath(this, syspath);
        }
    }
}

