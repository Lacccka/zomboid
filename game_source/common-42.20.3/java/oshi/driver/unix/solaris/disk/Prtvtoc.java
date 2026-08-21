/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.solaris.disk;

import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.HWPartition;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public final class Prtvtoc {
    private static final String PRTVTOC_DEV_DSK = "prtvtoc /dev/dsk/";

    private Prtvtoc() {
    }

    public static List<HWPartition> queryPartitions(String mount, int major) {
        ArrayList<HWPartition> partList = new ArrayList<HWPartition>();
        List<String> prtvotc = ExecutingCommand.runNative(PRTVTOC_DEV_DSK + mount);
        if (prtvotc.size() > 1) {
            int bytesPerSector = 0;
            for (String line : prtvotc) {
                String[] split;
                if (line.startsWith("*")) {
                    if (!line.endsWith("bytes/sector") || (split = ParseUtil.whitespaces.split(line)).length <= 0) continue;
                    bytesPerSector = ParseUtil.parseIntOrDefault(split[1], 0);
                    continue;
                }
                if (bytesPerSector <= 0 || (split = ParseUtil.whitespaces.split(line.trim())).length < 6 || "2".equals(split[0])) continue;
                String identification = mount + "s" + split[0];
                int minor = ParseUtil.parseIntOrDefault(split[0], 0);
                String name = switch (ParseUtil.parseIntOrDefault(split[1], 0)) {
                    case 1, 24 -> "boot";
                    case 2 -> "root";
                    case 3 -> "swap";
                    case 4 -> "usr";
                    case 5 -> "backup";
                    case 6 -> "stand";
                    case 7 -> "var";
                    case 8 -> "home";
                    case 9 -> "altsctr";
                    case 10 -> "cache";
                    case 11 -> "reserved";
                    case 12 -> "system";
                    case 14 -> "public region";
                    case 15 -> "private region";
                    default -> "unknown";
                };
                String type = switch (split[2]) {
                    case "00" -> "wm";
                    case "10" -> "rm";
                    case "01" -> "wu";
                    default -> "ru";
                };
                long partSize = (long)bytesPerSector * ParseUtil.parseLongOrDefault(split[4], 0L);
                String mountPoint = "";
                if (split.length > 6) {
                    mountPoint = split[6];
                }
                partList.add(new HWPartition(identification, name, type, "", partSize, major, minor, mountPoint));
            }
        }
        return partList;
    }
}

