/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.openbsd.disk;

import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.HWPartition;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Quartet;

@ThreadSafe
public final class Disklabel {
    private Disklabel() {
    }

    public static Quartet<String, String, Long, List<HWPartition>> getDiskParams(String diskName) {
        ArrayList<HWPartition> partitions = new ArrayList<HWPartition>();
        String totalMarker = "total sectors:";
        long totalSectors = 1L;
        String bpsMarker = "bytes/sector:";
        int bytesPerSector = 1;
        String labelMarker = "label:";
        String label = "";
        String duidMarker = "duid:";
        String duid = "";
        for (String line : ExecutingCommand.runNative("disklabel -n " + diskName)) {
            if (line.contains(totalMarker)) {
                totalSectors = ParseUtil.getFirstIntValue(line);
            } else if (line.contains(bpsMarker)) {
                bytesPerSector = ParseUtil.getFirstIntValue(line);
            } else if (line.contains(labelMarker)) {
                label = line.split(labelMarker)[1].trim();
            } else if (line.contains(duidMarker)) {
                duid = line.split(duidMarker)[1].trim();
            }
            if (line.trim().indexOf(58) != 1) continue;
            String[] split = ParseUtil.whitespaces.split(line.trim(), 9);
            String name = split[0].substring(0, 1);
            Pair<Integer, Integer> majorMinor = Disklabel.getMajorMinor(diskName, name);
            if (split.length <= 4) continue;
            partitions.add(new HWPartition(diskName + name, name, split[3], duid + "." + name, ParseUtil.parseLongOrDefault(split[1], 0L) * (long)bytesPerSector, majorMinor.getA(), majorMinor.getB(), split.length > 5 ? split[split.length - 1] : ""));
        }
        if (partitions.isEmpty()) {
            return Disklabel.getDiskParamsNoRoot(diskName);
        }
        return new Quartet<String, String, Long, List<HWPartition>>(label, duid, totalSectors * (long)bytesPerSector, partitions);
    }

    private static Quartet<String, String, Long, List<HWPartition>> getDiskParamsNoRoot(String diskName) {
        ArrayList<HWPartition> partitions = new ArrayList<HWPartition>();
        for (String line : ExecutingCommand.runNative("df")) {
            if (!line.startsWith("/dev/" + diskName)) continue;
            String[] split = ParseUtil.whitespaces.split(line);
            String name = split[0].substring(5 + diskName.length());
            Pair<Integer, Integer> majorMinor = Disklabel.getMajorMinor(diskName, name);
            if (split.length <= 5) continue;
            long partSize = ParseUtil.parseLongOrDefault(split[1], 1L) * 512L;
            partitions.add(new HWPartition(split[0], split[0].substring(5), "unknown", "unknown", partSize, majorMinor.getA(), majorMinor.getB(), split[5]));
        }
        return new Quartet<String, String, Long, List<HWPartition>>("unknown", "unknown", 0L, partitions);
    }

    private static Pair<Integer, Integer> getMajorMinor(String diskName, String name) {
        int major = 0;
        int minor = 0;
        String majorMinor = ExecutingCommand.getFirstAnswer("stat -f %Hr,%Lr /dev/" + diskName + name);
        int comma = majorMinor.indexOf(44);
        if (comma > 0 && comma < majorMinor.length()) {
            major = ParseUtil.parseIntOrDefault(majorMinor.substring(0, comma), 0);
            minor = ParseUtil.parseIntOrDefault(majorMinor.substring(comma + 1), 0);
        }
        return new Pair<Integer, Integer>(major, minor);
    }
}

