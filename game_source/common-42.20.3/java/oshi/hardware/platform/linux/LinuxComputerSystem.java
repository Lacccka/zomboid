/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.driver.linux.Devicetree;
import oshi.driver.linux.Dmidecode;
import oshi.driver.linux.Lshal;
import oshi.driver.linux.Lshw;
import oshi.driver.linux.Sysfs;
import oshi.driver.linux.proc.CpuInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.Firmware;
import oshi.hardware.common.AbstractComputerSystem;
import oshi.hardware.platform.linux.LinuxBaseboard;
import oshi.hardware.platform.linux.LinuxFirmware;
import oshi.util.Memoizer;

@Immutable
final class LinuxComputerSystem
extends AbstractComputerSystem {
    private final Supplier<String> manufacturer = Memoizer.memoize(LinuxComputerSystem::queryManufacturer);
    private final Supplier<String> model = Memoizer.memoize(LinuxComputerSystem::queryModel);
    private final Supplier<String> serialNumber = Memoizer.memoize(LinuxComputerSystem::querySerialNumber);
    private final Supplier<String> uuid = Memoizer.memoize(LinuxComputerSystem::queryUUID);

    LinuxComputerSystem() {
    }

    @Override
    public String getManufacturer() {
        return this.manufacturer.get();
    }

    @Override
    public String getModel() {
        return this.model.get();
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber.get();
    }

    @Override
    public String getHardwareUUID() {
        return this.uuid.get();
    }

    @Override
    public Firmware createFirmware() {
        return new LinuxFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new LinuxBaseboard();
    }

    private static String queryManufacturer() {
        String result = null;
        result = Sysfs.querySystemVendor();
        if (result == null && (result = CpuInfo.queryCpuManufacturer()) == null) {
            return "unknown";
        }
        return result;
    }

    private static String queryModel() {
        String result = null;
        result = Sysfs.queryProductModel();
        if (result == null && (result = Devicetree.queryModel()) == null && (result = Lshw.queryModel()) == null) {
            return "unknown";
        }
        return result;
    }

    private static String querySerialNumber() {
        String result = null;
        result = Sysfs.queryProductSerial();
        if (result == null && (result = Dmidecode.querySerialNumber()) == null && (result = Lshal.querySerialNumber()) == null && (result = Lshw.querySerialNumber()) == null) {
            return "unknown";
        }
        return result;
    }

    private static String queryUUID() {
        String result = null;
        result = Sysfs.queryUUID();
        if (result == null && (result = Dmidecode.queryUUID()) == null && (result = Lshal.queryUUID()) == null && (result = Lshw.queryUUID()) == null) {
            return "unknown";
        }
        return result;
    }
}

