/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.driver.linux.Sysfs;
import oshi.driver.linux.proc.CpuInfo;
import oshi.hardware.common.AbstractBaseboard;
import oshi.util.Memoizer;
import oshi.util.tuples.Quartet;

@Immutable
final class LinuxBaseboard
extends AbstractBaseboard {
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<String> model = Memoizer.memoize(this::queryModel);
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    private final Supplier<String> serialNumber = Memoizer.memoize(this::querySerialNumber);
    private final Supplier<Quartet<String, String, String, String>> manufacturerModelVersionSerial = Memoizer.memoize(CpuInfo::queryBoardInfo);

    LinuxBaseboard() {
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
    public String getVersion() {
        return this.version.get();
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber.get();
    }

    private String queryManufacturer() {
        String result = null;
        result = Sysfs.queryBoardVendor();
        if (result == null && (result = this.manufacturerModelVersionSerial.get().getA()) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryModel() {
        String result = null;
        result = Sysfs.queryBoardModel();
        if (result == null && (result = this.manufacturerModelVersionSerial.get().getB()) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryVersion() {
        String result = null;
        result = Sysfs.queryBoardVersion();
        if (result == null && (result = this.manufacturerModelVersionSerial.get().getC()) == null) {
            return "unknown";
        }
        return result;
    }

    private String querySerialNumber() {
        String result = null;
        result = Sysfs.queryBoardSerial();
        if (result == null && (result = this.manufacturerModelVersionSerial.get().getD()) == null) {
            return "unknown";
        }
        return result;
    }
}

