/*
 * Decompiled with CFR 0.152.
 */
package oshi.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;

@ThreadSafe
public final class ProcUtil {
    private ProcUtil() {
    }

    public static Map<String, Map<String, Long>> parseNestedStatistics(String procFile, String ... keys2) {
        HashMap<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
        List<String> keyList = Arrays.asList(keys2);
        List<String> lines = FileUtil.readFile(procFile);
        String previousKey = null;
        String[] statNames = null;
        for (String line : lines) {
            String[] parts = ParseUtil.whitespaces.split(line);
            if (parts.length == 0) continue;
            if (parts[0].isEmpty()) {
                parts = Arrays.copyOfRange(parts, 1, parts.length);
            }
            String key = parts[0].substring(0, parts[0].length() - 1);
            if (!keyList.isEmpty() && !keyList.contains(key)) continue;
            if (key.equals(previousKey)) {
                if (parts.length == statNames.length) {
                    HashMap<String, Long> stats = new HashMap<String, Long>(parts.length - 1);
                    for (int i = 1; i < parts.length; ++i) {
                        stats.put(statNames[i], ParseUtil.parseLongOrDefault(parts[i], 0L));
                    }
                    result.put(key, stats);
                }
            } else {
                statNames = parts;
            }
            previousKey = key;
        }
        return result;
    }

    public static Map<String, Long> parseStatistics(String procFile, Pattern separator) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        List<String> lines = FileUtil.readFile(procFile);
        for (String line : lines) {
            String[] parts = separator.split(line);
            if (parts[0].isEmpty()) {
                parts = Arrays.copyOfRange(parts, 1, parts.length);
            }
            if (parts.length != 2) continue;
            result.put(parts[0], ParseUtil.parseLongOrDefault(parts[1], 0L));
        }
        return result;
    }

    public static Map<String, Long> parseStatistics(String procFile) {
        return ProcUtil.parseStatistics(procFile, ParseUtil.whitespaces);
    }
}

