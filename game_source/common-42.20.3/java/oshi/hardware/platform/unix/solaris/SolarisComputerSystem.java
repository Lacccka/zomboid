/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Baseboard;
import oshi.hardware.Firmware;
import oshi.hardware.common.AbstractComputerSystem;
import oshi.hardware.platform.unix.UnixBaseboard;
import oshi.hardware.platform.unix.solaris.SolarisFirmware;
import oshi.util.ExecutingCommand;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.Util;

@Immutable
final class SolarisComputerSystem
extends AbstractComputerSystem {
    private final Supplier<SmbiosStrings> smbiosStrings = Memoizer.memoize(SolarisComputerSystem::readSmbios);

    SolarisComputerSystem() {
    }

    @Override
    public String getManufacturer() {
        return this.smbiosStrings.get().manufacturer;
    }

    @Override
    public String getModel() {
        return this.smbiosStrings.get().model;
    }

    @Override
    public String getSerialNumber() {
        return this.smbiosStrings.get().serialNumber;
    }

    @Override
    public String getHardwareUUID() {
        return this.smbiosStrings.get().uuid;
    }

    @Override
    public Firmware createFirmware() {
        return new SolarisFirmware(this.smbiosStrings.get().biosVendor, this.smbiosStrings.get().biosVersion, this.smbiosStrings.get().biosDate);
    }

    @Override
    public Baseboard createBaseboard() {
        return new UnixBaseboard(this.smbiosStrings.get().boardManufacturer, this.smbiosStrings.get().boardModel, this.smbiosStrings.get().boardSerialNumber, this.smbiosStrings.get().boardVersion);
    }

    private static SmbiosStrings readSmbios() {
        String serialNumMarker = "Serial Number";
        SmbType smbTypeId = null;
        EnumMap smbTypesMap = new EnumMap(SmbType.class);
        smbTypesMap.put(SmbType.SMB_TYPE_BIOS, new HashMap());
        smbTypesMap.put(SmbType.SMB_TYPE_SYSTEM, new HashMap());
        smbTypesMap.put(SmbType.SMB_TYPE_BASEBOARD, new HashMap());
        for (String checkLine : ExecutingCommand.runNative("smbios")) {
            if (checkLine.contains("SMB_TYPE_") && (smbTypeId = SolarisComputerSystem.getSmbType(checkLine)) == null) break;
            Integer colonDelimiterIndex = checkLine.indexOf(":");
            if (smbTypeId == null || colonDelimiterIndex < 0) continue;
            String key = checkLine.substring(0, colonDelimiterIndex).trim();
            String val = checkLine.substring(colonDelimiterIndex + 1).trim();
            ((Map)smbTypesMap.get((Object)smbTypeId)).put(key, val);
        }
        Map smbTypeBIOSMap = (Map)smbTypesMap.get((Object)SmbType.SMB_TYPE_BIOS);
        Map smbTypeSystemMap = (Map)smbTypesMap.get((Object)SmbType.SMB_TYPE_SYSTEM);
        Map smbTypeBaseboardMap = (Map)smbTypesMap.get((Object)SmbType.SMB_TYPE_BASEBOARD);
        if (!smbTypeSystemMap.containsKey("Serial Number") || Util.isBlank((String)smbTypeSystemMap.get("Serial Number"))) {
            smbTypeSystemMap.put("Serial Number", SolarisComputerSystem.readSerialNumber());
        }
        return new SmbiosStrings(smbTypeBIOSMap, smbTypeSystemMap, smbTypeBaseboardMap);
    }

    private static SmbType getSmbType(String checkLine) {
        for (SmbType smbType : SmbType.values()) {
            if (!checkLine.contains(smbType.name())) continue;
            return smbType;
        }
        return null;
    }

    private static String readSerialNumber() {
        String serialNumber = ExecutingCommand.getFirstAnswer("sneep");
        if (serialNumber.isEmpty()) {
            String marker = "chassis-sn:";
            for (String checkLine : ExecutingCommand.runNative("prtconf -pv")) {
                if (!checkLine.contains(marker)) continue;
                serialNumber = ParseUtil.getSingleQuoteStringValue(checkLine);
                break;
            }
        }
        return serialNumber;
    }

    private static final class SmbiosStrings {
        private final String biosVendor;
        private final String biosVersion;
        private final String biosDate;
        private final String manufacturer;
        private final String model;
        private final String serialNumber;
        private final String uuid;
        private final String boardManufacturer;
        private final String boardModel;
        private final String boardVersion;
        private final String boardSerialNumber;

        private SmbiosStrings(Map<String, String> smbTypeBIOSStrings, Map<String, String> smbTypeSystemStrings, Map<String, String> smbTypeBaseboardStrings) {
            String vendorMarker = "Vendor";
            String biosDateMarker = "Release Date";
            String biosVersionMarker = "Version String";
            String manufacturerMarker = "Manufacturer";
            String productMarker = "Product";
            String serialNumMarker = "Serial Number";
            String uuidMarker = "UUID";
            String versionMarker = "Version";
            this.biosVendor = ParseUtil.getValueOrUnknown(smbTypeBIOSStrings, "Vendor");
            this.biosVersion = ParseUtil.getValueOrUnknown(smbTypeBIOSStrings, "Version String");
            this.biosDate = ParseUtil.getValueOrUnknown(smbTypeBIOSStrings, "Release Date");
            this.manufacturer = ParseUtil.getValueOrUnknown(smbTypeSystemStrings, "Manufacturer");
            this.model = ParseUtil.getValueOrUnknown(smbTypeSystemStrings, "Product");
            this.serialNumber = ParseUtil.getValueOrUnknown(smbTypeSystemStrings, "Serial Number");
            this.uuid = ParseUtil.getValueOrUnknown(smbTypeSystemStrings, "UUID");
            this.boardManufacturer = ParseUtil.getValueOrUnknown(smbTypeBaseboardStrings, "Manufacturer");
            this.boardModel = ParseUtil.getValueOrUnknown(smbTypeBaseboardStrings, "Product");
            this.boardVersion = ParseUtil.getValueOrUnknown(smbTypeBaseboardStrings, "Version");
            this.boardSerialNumber = ParseUtil.getValueOrUnknown(smbTypeBaseboardStrings, "Serial Number");
        }
    }

    public static enum SmbType {
        SMB_TYPE_BIOS,
        SMB_TYPE_SYSTEM,
        SMB_TYPE_BASEBOARD;

    }
}

