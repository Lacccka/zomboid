/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.linux;

import java.io.File;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.GlobalConfig;

@ThreadSafe
public final class SysPath {
    public static final String SYS = SysPath.querySysConfig() + "/";
    public static final String CPU = SYS + "devices/system/cpu/";
    public static final String DMI_ID = SYS + "devices/virtual/dmi/id/";
    public static final String NET = SYS + "class/net/";
    public static final String MODEL = SYS + "firmware/devicetree/base/model";
    public static final String POWER_SUPPLY = SYS + "class/power_supply";
    public static final String HWMON = SYS + "class/hwmon/";
    public static final String THERMAL = SYS + "class/thermal/";

    private SysPath() {
    }

    private static String querySysConfig() {
        Object sysPath = GlobalConfig.get("oshi.util.sys.path", "/sys");
        if (!new File((String)(sysPath = "/" + ((String)sysPath).replaceAll("/$|^/", ""))).exists()) {
            throw new GlobalConfig.PropertyException("oshi.util.sys.path", "The path does not exist");
        }
        return sysPath;
    }
}

