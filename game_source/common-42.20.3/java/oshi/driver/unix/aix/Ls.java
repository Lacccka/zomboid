/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.aix;

import java.util.HashMap;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class Ls {
    private Ls() {
    }

    public static Map<String, Pair<Integer, Integer>> queryDeviceMajorMinor() {
        HashMap<String, Pair<Integer, Integer>> majMinMap = new HashMap<String, Pair<Integer, Integer>>();
        for (String s : ExecutingCommand.runNative("ls -l /dev")) {
            int idx;
            if (s.isEmpty() || s.charAt(0) != 'b' || (idx = s.lastIndexOf(32)) <= 0 || idx >= s.length()) continue;
            String device = s.substring(idx + 1);
            int major = ParseUtil.getNthIntValue(s, 2);
            int minor = ParseUtil.getNthIntValue(s, 3);
            majMinMap.put(device, new Pair<Integer, Integer>(major, minor));
        }
        return majMinMap;
    }
}

