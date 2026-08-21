/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import oshi.hardware.LogicalVolumeGroup;
import oshi.hardware.common.AbstractLogicalVolumeGroup;
import oshi.util.ExecutingCommand;

final class MacLogicalVolumeGroup
extends AbstractLogicalVolumeGroup {
    private static final String DISKUTIL_CS_LIST = "diskutil cs list";
    private static final String LOGICAL_VOLUME_GROUP = "Logical Volume Group";
    private static final String PHYSICAL_VOLUME = "Physical Volume";
    private static final String LOGICAL_VOLUME = "Logical Volume";

    MacLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        HashMap<String, Map> logicalVolumesMap = new HashMap<String, Map>();
        HashMap<String, Set> physicalVolumesMap = new HashMap<String, Set>();
        String currentVolumeGroup = null;
        boolean lookForVGName = false;
        boolean lookForPVName = false;
        for (String line : ExecutingCommand.runNative(DISKUTIL_CS_LIST)) {
            int indexOf;
            if (line.contains(LOGICAL_VOLUME_GROUP)) {
                lookForVGName = true;
                continue;
            }
            if (lookForVGName) {
                indexOf = line.indexOf("Name:");
                if (indexOf < 0) continue;
                currentVolumeGroup = line.substring(indexOf + 5).trim();
                lookForVGName = false;
                continue;
            }
            if (line.contains(PHYSICAL_VOLUME)) {
                lookForPVName = true;
                continue;
            }
            if (line.contains(LOGICAL_VOLUME)) {
                lookForPVName = false;
                continue;
            }
            indexOf = line.indexOf("Disk:");
            if (indexOf < 0) continue;
            if (lookForPVName) {
                physicalVolumesMap.computeIfAbsent(currentVolumeGroup, k -> new HashSet()).add(line.substring(indexOf + 5).trim());
                continue;
            }
            logicalVolumesMap.computeIfAbsent(currentVolumeGroup, k -> new HashMap()).put(line.substring(indexOf + 5).trim(), Collections.emptySet());
        }
        return logicalVolumesMap.entrySet().stream().map(e -> new MacLogicalVolumeGroup((String)e.getKey(), (Map)e.getValue(), (Set)physicalVolumesMap.get(e.getKey()))).collect(Collectors.toList());
    }
}

