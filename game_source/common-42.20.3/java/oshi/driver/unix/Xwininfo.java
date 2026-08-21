/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.OSDesktopWindow;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.Util;

@ThreadSafe
public final class Xwininfo {
    private static final String[] NET_CLIENT_LIST_STACKING = ParseUtil.whitespaces.split("xprop -root _NET_CLIENT_LIST_STACKING");
    private static final String[] XWININFO_ROOT_TREE = ParseUtil.whitespaces.split("xwininfo -root -tree");
    private static final String[] XPROP_NET_WM_PID_ID = ParseUtil.whitespaces.split("xprop _NET_WM_PID -id");

    private Xwininfo() {
    }

    public static List<OSDesktopWindow> queryXWindows(boolean visibleOnly) {
        String id;
        String stack;
        int bottom;
        HashMap<String, Integer> zOrderMap = new HashMap<String, Integer>();
        int z = 0;
        List<String> stacking = ExecutingCommand.runNative(NET_CLIENT_LIST_STACKING, null);
        if (!stacking.isEmpty() && (bottom = (stack = stacking.get(0)).indexOf("0x")) >= 0) {
            for (String id2 : stack.substring(bottom).split(", ")) {
                zOrderMap.put(id2, ++z);
            }
        }
        Pattern windowPattern = Pattern.compile("(0x\\S+) (?:\"(.+)\")?.*: \\((?:\"(.+)\" \".+\")?\\)  (\\d+)x(\\d+)\\+.+  \\+(-?\\d+)\\+(-?\\d+)");
        HashMap<String, String> windowNameMap = new HashMap<String, String>();
        HashMap<String, String> windowPathMap = new HashMap<String, String>();
        LinkedHashMap<String, Rectangle> windowMap = new LinkedHashMap<String, Rectangle>();
        for (String line : ExecutingCommand.runNative(XWININFO_ROOT_TREE, null)) {
            String windowPath;
            Matcher m = windowPattern.matcher(line.trim());
            if (!m.matches()) continue;
            id = m.group(1);
            if (visibleOnly && !zOrderMap.containsKey(id)) continue;
            String windowName = m.group(2);
            if (!Util.isBlank(windowName)) {
                windowNameMap.put(id, windowName);
            }
            if (!Util.isBlank(windowPath = m.group(3))) {
                windowPathMap.put(id, windowPath);
            }
            windowMap.put(id, new Rectangle(ParseUtil.parseIntOrDefault(m.group(6), 0), ParseUtil.parseIntOrDefault(m.group(7), 0), ParseUtil.parseIntOrDefault(m.group(4), 0), ParseUtil.parseIntOrDefault(m.group(5), 0)));
        }
        ArrayList<OSDesktopWindow> windowList = new ArrayList<OSDesktopWindow>();
        for (Map.Entry e : windowMap.entrySet()) {
            id = (String)e.getKey();
            long pid = Xwininfo.queryPidFromId(id);
            boolean visible = zOrderMap.containsKey(id);
            windowList.add(new OSDesktopWindow(ParseUtil.hexStringToLong(id, 0L), windowNameMap.getOrDefault(id, ""), windowPathMap.getOrDefault(id, ""), (Rectangle)e.getValue(), pid, zOrderMap.getOrDefault(id, 0), visible));
        }
        return windowList;
    }

    private static long queryPidFromId(String id) {
        String[] cmd = new String[XPROP_NET_WM_PID_ID.length + 1];
        System.arraycopy(XPROP_NET_WM_PID_ID, 0, cmd, 0, XPROP_NET_WM_PID_ID.length);
        cmd[Xwininfo.XPROP_NET_WM_PID_ID.length] = id;
        List<String> pidStr = ExecutingCommand.runNative(cmd, null);
        if (pidStr.isEmpty()) {
            return 0L;
        }
        return ParseUtil.getFirstIntValue(pidStr.get(0));
    }
}

