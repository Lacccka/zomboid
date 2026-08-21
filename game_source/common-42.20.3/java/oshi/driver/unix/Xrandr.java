/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public final class Xrandr {
    private static final String[] XRANDR_VERBOSE = new String[]{"xrandr", "--verbose"};

    private Xrandr() {
    }

    public static List<byte[]> getEdidArrays() {
        List<String> xrandr = ExecutingCommand.runNative(XRANDR_VERBOSE, null);
        if (xrandr.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<byte[]> displays = new ArrayList<byte[]>();
        StringBuilder sb = null;
        for (String s : xrandr) {
            if (s.contains("EDID")) {
                sb = new StringBuilder();
                continue;
            }
            if (sb == null) continue;
            sb.append(s.trim());
            if (sb.length() < 256) continue;
            String edidStr = sb.toString();
            byte[] edid = ParseUtil.hexStringToByteArray(edidStr);
            if (edid.length >= 128) {
                displays.add(edid);
            }
            sb = null;
        }
        return displays;
    }
}

