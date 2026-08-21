/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.freebsd.disk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class GeomDiskList {
    private static final String GEOM_DISK_LIST = "geom disk list";

    private GeomDiskList() {
    }

    public static Map<String, Triplet<String, String, Long>> queryDisks() {
        HashMap<String, Triplet<String, String, Long>> diskMap = new HashMap<String, Triplet<String, String, Long>>();
        String diskName = null;
        String descr = "unknown";
        String ident = "unknown";
        long mediaSize = 0L;
        List<String> geom = ExecutingCommand.runNative(GEOM_DISK_LIST);
        for (String line : geom) {
            String[] split;
            if ((line = line.trim()).startsWith("Geom name:")) {
                if (diskName != null) {
                    diskMap.put(diskName, new Triplet<String, String, Long>(descr, ident, mediaSize));
                    descr = "unknown";
                    ident = "unknown";
                    mediaSize = 0L;
                }
                diskName = line.substring(line.lastIndexOf(32) + 1);
            }
            if (diskName == null) continue;
            if ((line = line.trim()).startsWith("Mediasize:") && (split = ParseUtil.whitespaces.split(line)).length > 1) {
                mediaSize = ParseUtil.parseLongOrDefault(split[1], 0L);
            }
            if (line.startsWith("descr:")) {
                descr = line.replace("descr:", "").trim();
            }
            if (!line.startsWith("ident:")) continue;
            ident = line.replace("ident:", "").replace("(null)", "").trim();
        }
        if (diskName != null) {
            diskMap.put(diskName, new Triplet<String, String, Long>(descr, ident, mediaSize));
        }
        return diskMap;
    }
}

