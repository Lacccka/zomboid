/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.SetupApi;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Display;
import oshi.hardware.common.AbstractDisplay;
import oshi.jna.ByRef;
import oshi.jna.Struct;

@Immutable
final class WindowsDisplay
extends AbstractDisplay {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsDisplay.class);
    private static final SetupApi SU = SetupApi.INSTANCE;
    private static final Advapi32 ADV = Advapi32.INSTANCE;
    private static final Guid.GUID GUID_DEVINTERFACE_MONITOR = new Guid.GUID("E6F07B5F-EE97-4a90-B076-33F57BF4EAA7");

    WindowsDisplay(byte[] edid) {
        super(edid);
        LOG.debug("Initialized WindowsDisplay");
    }

    public static List<Display> getDisplays() {
        ArrayList<Display> displays = new ArrayList<Display>();
        WinNT.HANDLE hDevInfo = SU.SetupDiGetClassDevs(GUID_DEVINTERFACE_MONITOR, null, null, 18);
        if (!hDevInfo.equals(WinBase.INVALID_HANDLE_VALUE)) {
            try (Struct.CloseableSpDeviceInterfaceData deviceInterfaceData = new Struct.CloseableSpDeviceInterfaceData();
                 Struct.CloseableSpDevinfoData info = new Struct.CloseableSpDevinfoData();){
                deviceInterfaceData.cbSize = deviceInterfaceData.size();
                int memberIndex = 0;
                while (SU.SetupDiEnumDeviceInfo(hDevInfo, memberIndex, info)) {
                    WinReg.HKEY key = SU.SetupDiOpenDevRegKey(hDevInfo, info, 1, 0, 1, 1);
                    byte[] edid = new byte[1];
                    try (ByRef.CloseableIntByReference pType = new ByRef.CloseableIntByReference();
                         ByRef.CloseableIntByReference lpcbData = new ByRef.CloseableIntByReference();){
                        if (ADV.RegQueryValueEx(key, "EDID", 0, (IntByReference)pType, edid, (IntByReference)lpcbData) == 234 && ADV.RegQueryValueEx(key, "EDID", 0, (IntByReference)pType, edid = new byte[lpcbData.getValue()], (IntByReference)lpcbData) == 0) {
                            WindowsDisplay display = new WindowsDisplay(edid);
                            displays.add(display);
                        }
                    }
                    Advapi32.INSTANCE.RegCloseKey(key);
                    ++memberIndex;
                }
            }
            SU.SetupDiDestroyDeviceInfoList(hDevInfo);
        }
        return displays;
    }
}

