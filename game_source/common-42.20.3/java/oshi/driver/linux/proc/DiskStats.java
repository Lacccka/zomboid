/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;

@ThreadSafe
public final class DiskStats {
    private DiskStats() {
    }

    public static Map<String, Map<IoStat, Long>> getDiskStats() {
        HashMap<String, Map<IoStat, Long>> diskStatMap = new HashMap<String, Map<IoStat, Long>>();
        IoStat[] enumArray = (IoStat[])IoStat.class.getEnumConstants();
        List<String> diskStats = FileUtil.readFile(ProcPath.DISKSTATS);
        for (String stat : diskStats) {
            String[] split = ParseUtil.whitespaces.split(stat.trim());
            EnumMap<IoStat, Long> statMap = new EnumMap<IoStat, Long>(IoStat.class);
            String name = null;
            for (int i = 0; i < enumArray.length && i < split.length; ++i) {
                if (enumArray[i] == IoStat.NAME) {
                    name = split[i];
                    continue;
                }
                statMap.put(enumArray[i], ParseUtil.parseLongOrDefault(split[i], 0L));
            }
            if (name == null) continue;
            diskStatMap.put(name, statMap);
        }
        return diskStatMap;
    }

    public static enum IoStat {
        MAJOR,
        MINOR,
        NAME,
        READS,
        READS_MERGED,
        READS_SECTOR,
        READS_MS,
        WRITES,
        WRITES_MERGED,
        WRITES_SECTOR,
        WRITES_MS,
        IO_QUEUE_LENGTH,
        IO_MS,
        IO_MS_WEIGHTED,
        DISCARDS,
        DISCARDS_MERGED,
        DISCARDS_SECTOR,
        DISCARDS_MS,
        FLUSHES,
        FLUSHES_MS;

    }
}

