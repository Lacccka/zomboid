/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;

@ThreadSafe
public final class UpTime {
    private UpTime() {
    }

    public static double getSystemUptimeSeconds() {
        String uptime = FileUtil.getStringFromFile(ProcPath.UPTIME);
        int spaceIndex = uptime.indexOf(32);
        if (spaceIndex < 0) {
            return 0.0;
        }
        return ParseUtil.parseDoubleOrDefault(uptime.substring(0, spaceIndex), 0.0);
    }
}

