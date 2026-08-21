/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.driver.linux.Dmidecode;
import oshi.driver.linux.Sysfs;
import oshi.hardware.common.AbstractFirmware;
import oshi.util.ExecutingCommand;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;

@Immutable
final class LinuxFirmware
extends AbstractFirmware {
    private static final DateTimeFormatter VCGEN_FORMATTER = DateTimeFormatter.ofPattern("MMM d uuuu HH:mm:ss", Locale.ENGLISH);
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<String> description = Memoizer.memoize(this::queryDescription);
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    private final Supplier<String> releaseDate = Memoizer.memoize(this::queryReleaseDate);
    private final Supplier<String> name = Memoizer.memoize(this::queryName);
    private final Supplier<VcGenCmdStrings> vcGenCmd = Memoizer.memoize(LinuxFirmware::queryVcGenCmd);
    private final Supplier<Pair<String, String>> biosNameRev = Memoizer.memoize(Dmidecode::queryBiosNameRev);

    LinuxFirmware() {
    }

    @Override
    public String getManufacturer() {
        return this.manufacturer.get();
    }

    @Override
    public String getDescription() {
        return this.description.get();
    }

    @Override
    public String getVersion() {
        return this.version.get();
    }

    @Override
    public String getReleaseDate() {
        return this.releaseDate.get();
    }

    @Override
    public String getName() {
        return this.name.get();
    }

    private String queryManufacturer() {
        String result = null;
        result = Sysfs.queryBiosVendor();
        if (result == null && (result = this.vcGenCmd.get().manufacturer) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryDescription() {
        String result = null;
        result = Sysfs.queryBiosDescription();
        if (result == null && (result = this.vcGenCmd.get().description) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryVersion() {
        String result = null;
        result = Sysfs.queryBiosVersion(this.biosNameRev.get().getB());
        if (result == null && (result = this.vcGenCmd.get().version) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryReleaseDate() {
        String result = null;
        result = Sysfs.queryBiosReleaseDate();
        if (result == null && (result = this.vcGenCmd.get().releaseDate) == null) {
            return "unknown";
        }
        return result;
    }

    private String queryName() {
        String result = null;
        result = this.biosNameRev.get().getA();
        if (result == null && (result = this.vcGenCmd.get().name) == null) {
            return "unknown";
        }
        return result;
    }

    private static VcGenCmdStrings queryVcGenCmd() {
        String vcReleaseDate = null;
        String vcManufacturer = null;
        String vcVersion = null;
        List<String> vcgencmd = ExecutingCommand.runNative("vcgencmd version");
        if (vcgencmd.size() >= 3) {
            try {
                vcReleaseDate = DateTimeFormatter.ISO_LOCAL_DATE.format(VCGEN_FORMATTER.parse(vcgencmd.get(0)));
            }
            catch (DateTimeParseException e) {
                vcReleaseDate = "unknown";
            }
            String[] copyright = ParseUtil.whitespaces.split(vcgencmd.get(1));
            vcManufacturer = copyright[copyright.length - 1];
            vcVersion = vcgencmd.get(2).replace("version ", "");
            return new VcGenCmdStrings(vcReleaseDate, vcManufacturer, vcVersion, "RPi", "Bootloader");
        }
        return new VcGenCmdStrings(null, null, null, null, null);
    }

    private static final class VcGenCmdStrings {
        private final String releaseDate;
        private final String manufacturer;
        private final String version;
        private final String name;
        private final String description;

        private VcGenCmdStrings(String releaseDate, String manufacturer, String version, String name, String description) {
            this.releaseDate = releaseDate;
            this.manufacturer = manufacturer;
            this.version = version;
            this.name = name;
            this.description = description;
        }
    }
}

