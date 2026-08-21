/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.common.AbstractFirmware;
import oshi.util.Memoizer;
import oshi.util.Util;
import oshi.util.tuples.Quintet;

@Immutable
final class MacFirmware
extends AbstractFirmware {
    private final Supplier<Quintet<String, String, String, String, String>> manufNameDescVersRelease = Memoizer.memoize(MacFirmware::queryEfi);

    MacFirmware() {
    }

    @Override
    public String getManufacturer() {
        return this.manufNameDescVersRelease.get().getA();
    }

    @Override
    public String getName() {
        return this.manufNameDescVersRelease.get().getB();
    }

    @Override
    public String getDescription() {
        return this.manufNameDescVersRelease.get().getC();
    }

    @Override
    public String getVersion() {
        return this.manufNameDescVersRelease.get().getD();
    }

    @Override
    public String getReleaseDate() {
        return this.manufNameDescVersRelease.get().getE();
    }

    private static Quintet<String, String, String, String, String> queryEfi() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;
        IOKit.IOService platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data;
            IOKit.IOIterator iter = platformExpert.getChildIterator("IODeviceTree");
            if (iter != null) {
                IOKit.IORegistryEntry entry = iter.next();
                while (entry != null) {
                    switch (entry.getName()) {
                        case "rom": {
                            data = entry.getByteArrayProperty("vendor");
                            if (data != null) {
                                manufacturer = Native.toString(data, StandardCharsets.UTF_8);
                            }
                            if ((data = entry.getByteArrayProperty("version")) != null) {
                                version = Native.toString(data, StandardCharsets.UTF_8);
                            }
                            if ((data = entry.getByteArrayProperty("release-date")) == null) break;
                            releaseDate = Native.toString(data, StandardCharsets.UTF_8);
                            break;
                        }
                        case "chosen": {
                            data = entry.getByteArrayProperty("booter-name");
                            if (data == null) break;
                            name = Native.toString(data, StandardCharsets.UTF_8);
                            break;
                        }
                        case "efi": {
                            data = entry.getByteArrayProperty("firmware-abi");
                            if (data == null) break;
                            description = Native.toString(data, StandardCharsets.UTF_8);
                            break;
                        }
                        default: {
                            if (!Util.isBlank(name)) break;
                            name = entry.getStringProperty("IONameMatch");
                        }
                    }
                    entry.release();
                    entry = iter.next();
                }
                iter.release();
            }
            if (Util.isBlank(manufacturer) && (data = platformExpert.getByteArrayProperty("manufacturer")) != null) {
                manufacturer = Native.toString(data, StandardCharsets.UTF_8);
            }
            if (Util.isBlank(version) && (data = platformExpert.getByteArrayProperty("target-type")) != null) {
                version = Native.toString(data, StandardCharsets.UTF_8);
            }
            if (Util.isBlank(name) && (data = platformExpert.getByteArrayProperty("device_type")) != null) {
                name = Native.toString(data, StandardCharsets.UTF_8);
            }
            platformExpert.release();
        }
        return new Quintet<String, String, String, String, String>(Util.isBlank(manufacturer) ? "unknown" : manufacturer, Util.isBlank(name) ? "unknown" : name, Util.isBlank(description) ? "unknown" : description, Util.isBlank(version) ? "unknown" : version, Util.isBlank(releaseDate) ? "unknown" : releaseDate);
    }
}

