/*
 * Decompiled with CFR 0.152.
 */
package oshi;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.function.Supplier;
import oshi.PlatformEnumFFM;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.platform.linux.LinuxHardwareAbstractionLayer;
import oshi.hardware.platform.mac.MacHardwareAbstractionLayerFFM;
import oshi.hardware.platform.windows.WindowsHardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.software.os.mac.MacOperatingSystemFFM;
import oshi.software.os.windows.WindowsOperatingSystemFFM;
import oshi.util.Memoizer;

public class SystemInfoFFM {
    private static final PlatformEnumFFM CURRENT_PLATFORM;
    private static final String NOT_SUPPORTED;
    private final Supplier<OperatingSystem> os = Memoizer.memoize(SystemInfoFFM::createOperatingSystem);
    private final Supplier<HardwareAbstractionLayer> hardware = Memoizer.memoize(SystemInfoFFM::createHardware);

    private static OperatingSystem createOperatingSystem() {
        return switch (CURRENT_PLATFORM) {
            case PlatformEnumFFM.LINUX -> new LinuxOperatingSystem();
            case PlatformEnumFFM.MACOS -> new MacOperatingSystemFFM();
            case PlatformEnumFFM.WINDOWS -> new WindowsOperatingSystemFFM();
            default -> throw new UnsupportedOperationException(NOT_SUPPORTED);
        };
    }

    private static HardwareAbstractionLayer createHardware() {
        return switch (CURRENT_PLATFORM) {
            case PlatformEnumFFM.LINUX -> new LinuxHardwareAbstractionLayer();
            case PlatformEnumFFM.MACOS -> new MacHardwareAbstractionLayerFFM();
            case PlatformEnumFFM.WINDOWS -> new WindowsHardwareAbstractionLayer();
            default -> throw new UnsupportedOperationException(NOT_SUPPORTED);
        };
    }

    public static PlatformEnumFFM getCurrentPlatform() {
        return CURRENT_PLATFORM;
    }

    public OperatingSystem getOperatingSystem() {
        return this.os.get();
    }

    public HardwareAbstractionLayer getHardware() {
        return this.hardware.get();
    }

    static {
        PlatformEnumFFM platformEnumFFM;
        String osName;
        String string = osName = System.getProperty("os.name");
        Objects.requireNonNull(string);
        String string2 = string;
        int n = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{String.class, String.class, String.class}, (String)string2, n)) {
                case 0: {
                    String name = string2;
                    if (!name.startsWith("Linux")) {
                        n = 1;
                        continue block5;
                    }
                    platformEnumFFM = PlatformEnumFFM.LINUX;
                    break block5;
                }
                case 1: {
                    String name = string2;
                    if (!name.startsWith("Mac") && !name.startsWith("Darwin")) {
                        n = 2;
                        continue block5;
                    }
                    platformEnumFFM = PlatformEnumFFM.MACOS;
                    break block5;
                }
                case 2: {
                    String name = string2;
                    if (!name.startsWith("Windows")) {
                        n = 3;
                        continue block5;
                    }
                    platformEnumFFM = PlatformEnumFFM.WINDOWS;
                    break block5;
                }
                default: {
                    platformEnumFFM = PlatformEnumFFM.UNSUPPORTED;
                    break block5;
                }
            }
            break;
        }
        CURRENT_PLATFORM = platformEnumFFM;
        NOT_SUPPORTED = "Unsupported platform: " + String.valueOf((Object)CURRENT_PLATFORM);
    }
}

