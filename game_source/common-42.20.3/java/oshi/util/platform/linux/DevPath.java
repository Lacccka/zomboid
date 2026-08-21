/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.linux;

import java.io.File;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.GlobalConfig;

@ThreadSafe
public final class DevPath {
    public static final String DEV = DevPath.queryDevConfig() + "/";
    public static final String DISK_BY_UUID = DEV + "disk/by-uuid";
    public static final String DM = DEV + "dm";
    public static final String LOOP = DEV + "loop";
    public static final String MAPPER = DEV + "mapper/";
    public static final String RAM = DEV + "ram";

    private DevPath() {
    }

    private static String queryDevConfig() {
        Object devPath = GlobalConfig.get("oshi.util.dev.path", "/dev");
        if (!new File((String)(devPath = "/" + ((String)devPath).replaceAll("/$|^/", ""))).exists()) {
            throw new GlobalConfig.PropertyException("oshi.util.dev.path", "The path does not exist");
        }
        return devPath;
    }
}

