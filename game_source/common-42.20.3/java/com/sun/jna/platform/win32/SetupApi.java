/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface SetupApi
extends StdCallLibrary {
    public static final SetupApi INSTANCE = Native.load("setupapi", SetupApi.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final Guid.GUID GUID_DEVINTERFACE_DISK = new Guid.GUID("53F56307-B6BF-11D0-94F2-00A0C91EFB8B");
    public static final Guid.GUID GUID_DEVINTERFACE_COMPORT = new Guid.GUID("86E0D1E0-8089-11D0-9CE4-08003E301F73");
    public static final int DIGCF_DEFAULT = 1;
    public static final int DIGCF_PRESENT = 2;
    public static final int DIGCF_ALLCLASSES = 4;
    public static final int DIGCF_PROFILE = 8;
    public static final int DIGCF_DEVICEINTERFACE = 16;
    public static final int SPDRP_REMOVAL_POLICY = 31;
    public static final int CM_DEVCAP_REMOVABLE = 4;
    public static final int DICS_FLAG_GLOBAL = 1;
    public static final int DICS_FLAG_CONFIGSPECIFIC = 2;
    public static final int DICS_FLAG_CONFIGGENERAL = 4;
    public static final int DIREG_DEV = 1;
    public static final int DIREG_DRV = 2;
    public static final int DIREG_BOTH = 4;
    public static final int SPDRP_DEVICEDESC = 0;

    public WinNT.HANDLE SetupDiGetClassDevs(Guid.GUID var1, Pointer var2, Pointer var3, int var4);

    public boolean SetupDiDestroyDeviceInfoList(WinNT.HANDLE var1);

    public boolean SetupDiEnumDeviceInterfaces(WinNT.HANDLE var1, Pointer var2, Guid.GUID var3, int var4, SP_DEVICE_INTERFACE_DATA var5);

    public boolean SetupDiGetDeviceInterfaceDetail(WinNT.HANDLE var1, SP_DEVICE_INTERFACE_DATA var2, Pointer var3, int var4, IntByReference var5, SP_DEVINFO_DATA var6);

    public boolean SetupDiGetDeviceRegistryProperty(WinNT.HANDLE var1, SP_DEVINFO_DATA var2, int var3, IntByReference var4, Pointer var5, int var6, IntByReference var7);

    public WinReg.HKEY SetupDiOpenDevRegKey(WinNT.HANDLE var1, SP_DEVINFO_DATA var2, int var3, int var4, int var5, int var6);

    public boolean SetupDiEnumDeviceInfo(WinNT.HANDLE var1, int var2, SP_DEVINFO_DATA var3);

    @Structure.FieldOrder(value={"cbSize", "InterfaceClassGuid", "DevInst", "Reserved"})
    public static class SP_DEVINFO_DATA
    extends Structure {
        public int cbSize;
        public Guid.GUID InterfaceClassGuid;
        public int DevInst;
        public Pointer Reserved;

        public SP_DEVINFO_DATA() {
            this.cbSize = this.size();
        }

        public SP_DEVINFO_DATA(Pointer memory) {
            super(memory);
            this.read();
        }

        public static class ByReference
        extends SP_DEVINFO_DATA
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }

    @Structure.FieldOrder(value={"cbSize", "InterfaceClassGuid", "Flags", "Reserved"})
    public static class SP_DEVICE_INTERFACE_DATA
    extends Structure {
        public int cbSize;
        public Guid.GUID InterfaceClassGuid;
        public int Flags;
        public Pointer Reserved;

        public SP_DEVICE_INTERFACE_DATA() {
            this.cbSize = this.size();
        }

        public SP_DEVICE_INTERFACE_DATA(Pointer memory) {
            super(memory);
            this.read();
        }

        public static class ByReference
        extends SP_DEVINFO_DATA
        implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }
    }
}

