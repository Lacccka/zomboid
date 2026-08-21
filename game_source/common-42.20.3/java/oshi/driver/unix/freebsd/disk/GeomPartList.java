/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.freebsd.disk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.unix.freebsd.disk.Mount;
import oshi.hardware.HWPartition;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public final class GeomPartList {
    private static final String GEOM_PART_LIST = "geom part list";
    private static final String STAT_FILESIZE = "stat -f %i /dev/";

    private GeomPartList() {
    }

    public static Map<String, List<HWPartition>> queryPartitions() {
        Map<String, String> mountMap = Mount.queryPartitionToMountMap();
        HashMap<String, List<HWPartition>> partitionMap = new HashMap<String, List<HWPartition>>();
        String diskName = null;
        List<Object> partList = new ArrayList<HWPartition>();
        String partName = null;
        String identification = "unknown";
        String type = "unknown";
        String uuid = "unknown";
        long size = 0L;
        String mountPoint = "";
        List<String> geom = ExecutingCommand.runNative(GEOM_PART_LIST);
        for (String line : geom) {
            String[] split;
            if ((line = line.trim()).startsWith("Geom name:")) {
                if (diskName != null && !partList.isEmpty()) {
                    partitionMap.put(diskName, partList);
                    partList = new ArrayList();
                }
                diskName = line.substring(line.lastIndexOf(32) + 1);
            }
            if (diskName == null) continue;
            if (line.contains("Name:")) {
                String part;
                if (partName != null) {
                    int minor = ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer(STAT_FILESIZE + partName), 0);
                    partList.add(new HWPartition(identification, partName, type, uuid, size, 0, minor, mountPoint));
                    partName = null;
                    identification = "unknown";
                    type = "unknown";
                    uuid = "unknown";
                    size = 0L;
                }
                if ((part = line.substring(line.lastIndexOf(32) + 1)).startsWith(diskName)) {
                    partName = part;
                    identification = part;
                    mountPoint = mountMap.getOrDefault(part, "");
                }
            }
            if (partName == null || (split = ParseUtil.whitespaces.split(line)).length < 2) continue;
            if (line.startsWith("Mediasize:")) {
                size = ParseUtil.parseLongOrDefault(split[1], 0L);
                continue;
            }
            if (line.startsWith("rawuuid:")) {
                uuid = split[1];
                continue;
            }
            if (!line.startsWith("type:")) continue;
            type = split[1];
        }
        if (diskName != null) {
            if (partName != null) {
                int minor = ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer(STAT_FILESIZE + partName), 0);
                partList.add(new HWPartition(identification, partName, type, uuid, size, 0, minor, mountPoint));
            }
            if (!partList.isEmpty()) {
                partList = partList.stream().sorted(Comparator.comparing(HWPartition::getName)).collect(Collectors.toList());
                partitionMap.put(diskName, partList);
            }
        }
        return partitionMap;
    }
}

