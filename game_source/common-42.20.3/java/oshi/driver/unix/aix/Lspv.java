/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.aix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.HWPartition;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class Lspv {
    private static final Map<String, List<HWPartition>> PARTITION_CACHE = new ConcurrentHashMap<String, List<HWPartition>>();

    private Lspv() {
    }

    public static List<HWPartition> queryLogicalVolumes(String device, Map<String, Pair<Integer, Integer>> majMinMap) {
        return PARTITION_CACHE.computeIfAbsent(device, d -> Collections.unmodifiableList(Lspv.computeLogicalVolumes(d, majMinMap).stream().sorted(Comparator.comparing(HWPartition::getMinor).thenComparing(HWPartition::getName)).collect(Collectors.toList())));
    }

    private static List<HWPartition> computeLogicalVolumes(String device, Map<String, Pair<Integer, Integer>> majMinMap) {
        String name;
        ArrayList<HWPartition> partitions = new ArrayList<HWPartition>();
        String stateMarker = "PV STATE:";
        String sizeMarker = "PP SIZE:";
        long ppSize = 0L;
        for (String s : ExecutingCommand.runNative("lspv -L " + device)) {
            if (s.startsWith(stateMarker)) {
                if (s.contains("active")) continue;
                return partitions;
            }
            if (!s.contains(sizeMarker)) continue;
            ppSize = ParseUtil.getFirstIntValue(s);
        }
        if (ppSize == 0L) {
            return partitions;
        }
        ppSize <<= 20;
        HashMap<String, String> mountMap = new HashMap<String, String>();
        HashMap<String, String> typeMap = new HashMap<String, String>();
        HashMap<String, Integer> ppMap = new HashMap<String, Integer>();
        for (String string : ExecutingCommand.runNative("lspv -p " + device)) {
            String[] split = ParseUtil.whitespaces.split(string.trim());
            if (split.length < 6 || !"used".equals(split[1])) continue;
            name = split[split.length - 3];
            mountMap.put(name, split[split.length - 1]);
            typeMap.put(name, split[split.length - 2]);
            int ppCount = 1 + ParseUtil.getNthIntValue(split[0], 2) - ParseUtil.getNthIntValue(split[0], 1);
            ppMap.put(name, ppCount + ppMap.getOrDefault(name, 0));
        }
        for (Map.Entry entry : mountMap.entrySet()) {
            String mount = "N/A".equals(entry.getValue()) ? "" : (String)entry.getValue();
            name = (String)entry.getKey();
            String type = (String)typeMap.get(name);
            long size = ppSize * (long)((Integer)ppMap.get(name)).intValue();
            Pair<Integer, Integer> majMin = majMinMap.get(name);
            int major = majMin == null ? ParseUtil.getFirstIntValue(name) : majMin.getA();
            int minor = majMin == null ? ParseUtil.getFirstIntValue(name) : majMin.getB();
            partitions.add(new HWPartition(name, name, type, "", size, major, minor, mount));
        }
        return partitions;
    }
}

