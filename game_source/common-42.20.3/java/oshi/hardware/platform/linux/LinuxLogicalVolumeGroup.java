/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import com.sun.jna.platform.linux.Udev;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.LogicalVolumeGroup;
import oshi.hardware.common.AbstractLogicalVolumeGroup;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.linux.DevPath;

final class LinuxLogicalVolumeGroup
extends AbstractLogicalVolumeGroup {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxLogicalVolumeGroup.class);
    private static final String BLOCK = "block";
    private static final String DM_UUID = "DM_UUID";
    private static final String DM_VG_NAME = "DM_VG_NAME";
    private static final String DM_LV_NAME = "DM_LV_NAME";

    LinuxLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        if (!LinuxOperatingSystem.HAS_UDEV) {
            LOG.warn("Logical Volume Group information requires libudev, which is not present.");
            return Collections.emptyList();
        }
        HashMap<String, Map> logicalVolumesMap = new HashMap<String, Map>();
        HashMap<String, Set> physicalVolumesMap = new HashMap<String, Set>();
        for (String s : ExecutingCommand.runNative("pvs -o vg_name,pv_name")) {
            String[] split = ParseUtil.whitespaces.split(s.trim());
            if (split.length != 2 || !split[1].startsWith(DevPath.DEV)) continue;
            physicalVolumesMap.computeIfAbsent(split[0], k -> new HashSet()).add(split[1]);
        }
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem(BLOCK);
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    Udev.UdevDevice device = udev.deviceNewFromSyspath(syspath);
                    if (device == null) continue;
                    try {
                        String uuid;
                        String devnode = device.getDevnode();
                        if (devnode == null || !devnode.startsWith(DevPath.DM) || (uuid = device.getPropertyValue(DM_UUID)) == null || !uuid.startsWith("LVM-")) continue;
                        String vgName = device.getPropertyValue(DM_VG_NAME);
                        String lvName = device.getPropertyValue(DM_LV_NAME);
                        if (Util.isBlank(vgName) || Util.isBlank(lvName)) continue;
                        logicalVolumesMap.computeIfAbsent(vgName, k -> new HashMap());
                        Map lvMapForGroup = (Map)logicalVolumesMap.get(vgName);
                        physicalVolumesMap.computeIfAbsent(vgName, k -> new HashSet());
                        Set pvSetForGroup = (Set)physicalVolumesMap.get(vgName);
                        File slavesDir = new File(syspath + "/slaves");
                        File[] slaves = slavesDir.listFiles();
                        if (slaves == null) continue;
                        for (File f : slaves) {
                            String pvName = f.getName();
                            lvMapForGroup.computeIfAbsent(lvName, k -> new HashSet()).add(DevPath.DEV + pvName);
                            pvSetForGroup.add(DevPath.DEV + pvName);
                        }
                        continue;
                    }
                    finally {
                        device.unref();
                    }
                }
            }
            finally {
                enumerate.unref();
            }
        }
        finally {
            udev.unref();
        }
        return logicalVolumesMap.entrySet().stream().map(e -> new LinuxLogicalVolumeGroup((String)e.getKey(), (Map)e.getValue(), (Set)physicalVolumesMap.get(e.getKey()))).collect(Collectors.toList());
    }
}

