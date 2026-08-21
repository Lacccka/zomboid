/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.freebsd.disk;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;

@ThreadSafe
public final class Mount {
    private static final String MOUNT_CMD = "mount";
    private static final Pattern MOUNT_PATTERN = Pattern.compile("/dev/(\\S+p\\d+) on (\\S+) .*");

    private Mount() {
    }

    public static Map<String, String> queryPartitionToMountMap() {
        HashMap<String, String> mountMap = new HashMap<String, String>();
        for (String mnt : ExecutingCommand.runNative(MOUNT_CMD)) {
            Matcher m = MOUNT_PATTERN.matcher(mnt);
            if (!m.matches()) continue;
            mountMap.put(m.group(1), m.group(2));
        }
        return mountMap;
    }
}

