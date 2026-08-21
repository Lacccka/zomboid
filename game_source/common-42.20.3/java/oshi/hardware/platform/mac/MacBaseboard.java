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
import oshi.hardware.common.AbstractBaseboard;
import oshi.util.Memoizer;
import oshi.util.Util;
import oshi.util.tuples.Quartet;

@Immutable
final class MacBaseboard
extends AbstractBaseboard {
    private final Supplier<Quartet<String, String, String, String>> manufModelVersSerial = Memoizer.memoize(MacBaseboard::queryPlatform);

    MacBaseboard() {
    }

    @Override
    public String getManufacturer() {
        return this.manufModelVersSerial.get().getA();
    }

    @Override
    public String getModel() {
        return this.manufModelVersSerial.get().getB();
    }

    @Override
    public String getVersion() {
        return this.manufModelVersSerial.get().getC();
    }

    @Override
    public String getSerialNumber() {
        return this.manufModelVersSerial.get().getD();
    }

    private static Quartet<String, String, String, String> queryPlatform() {
        String manufacturer = null;
        String model = null;
        String version = null;
        String serialNumber = null;
        IOKit.IOService platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, StandardCharsets.UTF_8);
            }
            if ((data = platformExpert.getByteArrayProperty("board-id")) != null) {
                model = Native.toString(data, StandardCharsets.UTF_8);
            }
            if (Util.isBlank(model) && (data = platformExpert.getByteArrayProperty("model-number")) != null) {
                model = Native.toString(data, StandardCharsets.UTF_8);
            }
            if ((data = platformExpert.getByteArrayProperty("version")) != null) {
                version = Native.toString(data, StandardCharsets.UTF_8);
            }
            if ((data = platformExpert.getByteArrayProperty("mlb-serial-number")) != null) {
                serialNumber = Native.toString(data, StandardCharsets.UTF_8);
            }
            if (Util.isBlank(serialNumber)) {
                serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            }
            platformExpert.release();
        }
        return new Quartet<String, String, String, String>(Util.isBlank(manufacturer) ? "Apple Inc." : manufacturer, Util.isBlank(model) ? "unknown" : model, Util.isBlank(version) ? "unknown" : version, Util.isBlank(serialNumber) ? "unknown" : serialNumber);
    }
}

