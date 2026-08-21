/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.driver.windows.wmi.Win32VideoController;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.windows.RegistryUtil;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Triplet;

@Immutable
final class WindowsGraphicsCard
extends AbstractGraphicsCard {
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    public static final String ADAPTER_STRING = "HardwareInformation.AdapterString";
    public static final String DRIVER_DESC = "DriverDesc";
    public static final String DRIVER_VERSION = "DriverVersion";
    public static final String VENDOR = "ProviderName";
    public static final String QW_MEMORY_SIZE = "HardwareInformation.qwMemorySize";
    public static final String MEMORY_SIZE = "HardwareInformation.MemorySize";
    public static final String DISPLAY_DEVICES_REGISTRY_PATH = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\";

    WindowsGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    public static List<GraphicsCard> getGraphicsCards() {
        String[] keys2;
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        int index = 1;
        for (String key : keys2 = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, DISPLAY_DEVICES_REGISTRY_PATH)) {
            if (!key.startsWith("0")) continue;
            try {
                String memKey;
                String fullKey = DISPLAY_DEVICES_REGISTRY_PATH + key;
                if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, ADAPTER_STRING)) continue;
                String name = RegistryUtil.getStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, DRIVER_DESC);
                String deviceId = "VideoController" + index++;
                String vendor = RegistryUtil.getStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, VENDOR);
                String versionInfo = RegistryUtil.getStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, DRIVER_VERSION);
                long vram = 0L;
                String string = Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, QW_MEMORY_SIZE) ? QW_MEMORY_SIZE : (memKey = Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, MEMORY_SIZE) ? MEMORY_SIZE : null);
                if (memKey != null) {
                    Object genericValue = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, memKey);
                    if (genericValue instanceof Long) {
                        vram = (Long)genericValue;
                    } else if (genericValue instanceof Integer) {
                        vram = Integer.toUnsignedLong((Integer)genericValue);
                    } else if (genericValue instanceof byte[]) {
                        byte[] bytes = (byte[])genericValue;
                        vram = ParseUtil.byteArrayToLong(bytes, bytes.length, false);
                    }
                }
                cardList.add(new WindowsGraphicsCard(Util.isBlank(name) ? "unknown" : name, (String)(Util.isBlank(deviceId) ? "unknown" : deviceId), Util.isBlank(vendor) ? "unknown" : vendor, Util.isBlank(versionInfo) ? "unknown" : versionInfo, vram));
            }
            catch (Win32Exception e) {
                if (e.getErrorCode() == 5) continue;
                throw e;
            }
        }
        if (cardList.isEmpty()) {
            return WindowsGraphicsCard.getGraphicsCardsFromWmi();
        }
        return cardList;
    }

    private static List<GraphicsCard> getGraphicsCardsFromWmi() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        if (IS_VISTA_OR_GREATER) {
            WbemcliUtil.WmiResult<Win32VideoController.VideoControllerProperty> cards = Win32VideoController.queryVideoController();
            for (int index = 0; index < cards.getResultCount(); ++index) {
                Object versionInfo;
                String name = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.NAME, index);
                Triplet<String, String, String> idPair = ParseUtil.parseDeviceIdToVendorProductSerial(WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.PNPDEVICEID, index));
                String deviceId = idPair == null ? "unknown" : idPair.getB();
                Object vendor = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.ADAPTERCOMPATIBILITY, index);
                if (idPair != null) {
                    if (Util.isBlank((String)vendor)) {
                        deviceId = idPair.getA();
                    } else {
                        vendor = (String)vendor + " (" + idPair.getA() + ")";
                    }
                }
                versionInfo = !Util.isBlank((String)(versionInfo = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.DRIVERVERSION, index))) ? "DriverVersion=" + (String)versionInfo : "unknown";
                long vram = WmiUtil.getUint32asLong(cards, Win32VideoController.VideoControllerProperty.ADAPTERRAM, index);
                cardList.add(new WindowsGraphicsCard(Util.isBlank(name) ? "unknown" : name, deviceId, (String)(Util.isBlank((String)vendor) ? "unknown" : vendor), (String)versionInfo, vram));
            }
        }
        return cardList;
    }
}

