/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.solaris.disk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Quintet;

@ThreadSafe
public final class Iostat {
    private static final String IOSTAT_ER_DETAIL = "iostat -Er";
    private static final String IOSTAT_ER = "iostat -er";
    private static final String IOSTAT_ERN = "iostat -ern";
    private static final String DEVICE_HEADER = "device";

    private Iostat() {
    }

    public static Map<String, String> queryPartitionToMountMap() {
        HashMap<String, String> deviceMap = new HashMap<String, String>();
        List<String> mountNames = ExecutingCommand.runNative(IOSTAT_ER);
        List<String> mountPoints = ExecutingCommand.runNative(IOSTAT_ERN);
        for (int i = 0; i < mountNames.size() && i < mountPoints.size(); ++i) {
            String mount;
            String[] mountSplit;
            String disk = mountNames.get(i);
            String[] diskSplit = disk.split(",");
            if (diskSplit.length < 5 || DEVICE_HEADER.equals(diskSplit[0]) || (mountSplit = (mount = mountPoints.get(i)).split(",")).length < 5 || DEVICE_HEADER.equals(mountSplit[4])) continue;
            deviceMap.put(diskSplit[0], mountSplit[4]);
        }
        return deviceMap;
    }

    public static Map<String, Quintet<String, String, String, String, Long>> queryDeviceStrings(Set<String> diskSet) {
        HashMap<String, Quintet<String, String, String, String, Long>> deviceParamMap = new HashMap<String, Quintet<String, String, String, String, Long>>();
        List<String> iostat = ExecutingCommand.runNative(IOSTAT_ER_DETAIL);
        String diskName = null;
        String model = "";
        String vendor = "";
        String product = "";
        String serial = "";
        long size = 0L;
        for (String line : iostat) {
            String[] split;
            for (String keyValue : split = line.split(",")) {
                String[] bytes;
                if (diskSet.contains(keyValue = keyValue.trim())) {
                    if (diskName != null) {
                        deviceParamMap.put(diskName, new Quintet<String, String, String, String, Long>(model, vendor, product, serial, size));
                    }
                    diskName = keyValue;
                    model = "";
                    vendor = "";
                    product = "";
                    serial = "";
                    size = 0L;
                    continue;
                }
                if (keyValue.startsWith("Model:")) {
                    model = keyValue.replace("Model:", "").trim();
                    continue;
                }
                if (keyValue.startsWith("Serial No:")) {
                    serial = keyValue.replace("Serial No:", "").trim();
                    continue;
                }
                if (keyValue.startsWith("Vendor:")) {
                    vendor = keyValue.replace("Vendor:", "").trim();
                    continue;
                }
                if (keyValue.startsWith("Product:")) {
                    product = keyValue.replace("Product:", "").trim();
                    continue;
                }
                if (!keyValue.startsWith("Size:") || (bytes = keyValue.split("<")).length <= 1) continue;
                bytes = ParseUtil.whitespaces.split(bytes[1]);
                size = ParseUtil.parseLongOrDefault(bytes[0], 0L);
            }
            if (diskName == null) continue;
            deviceParamMap.put(diskName, new Quintet<String, String, String, String, Long>(model, vendor, product, serial, size));
        }
        return deviceParamMap;
    }
}

