/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.unix.freebsd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public final class ProcstatUtil {
    private ProcstatUtil() {
    }

    public static Map<Integer, String> getCwdMap(int pid) {
        List<String> procstat = ExecutingCommand.runNative("procstat -f " + String.valueOf(pid < 0 ? "-a" : Integer.valueOf(pid)));
        HashMap<Integer, String> cwdMap = new HashMap<Integer, String>();
        for (String line : procstat) {
            String[] split = ParseUtil.whitespaces.split(line.trim(), 10);
            if (split.length != 10 || !split[2].equals("cwd")) continue;
            cwdMap.put(ParseUtil.parseIntOrDefault(split[0], -1), split[9]);
        }
        return cwdMap;
    }

    public static String getCwd(int pid) {
        List<String> procstat = ExecutingCommand.runNative("procstat -f " + pid);
        for (String line : procstat) {
            String[] split = ParseUtil.whitespaces.split(line.trim(), 10);
            if (split.length != 10 || !split[2].equals("cwd")) continue;
            return split[9];
        }
        return "";
    }

    public static long getOpenFiles(int pid) {
        long fd = 0L;
        List<String> procstat = ExecutingCommand.runNative("procstat -f " + pid);
        for (String line : procstat) {
            String[] split = ParseUtil.whitespaces.split(line.trim(), 10);
            if (split.length != 10 || "Vd-".contains(split[4])) continue;
            ++fd;
        }
        return fd;
    }
}

