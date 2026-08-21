/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.platform.linux.ProcPath;

@ThreadSafe
public final class Auxv {
    public static final int AT_PAGESZ = 6;
    public static final int AT_HWCAP = 16;
    public static final int AT_CLKTCK = 17;

    private Auxv() {
    }

    public static Map<Integer, Long> queryAuxv() {
        int key;
        ByteBuffer buff = FileUtil.readAllBytesAsBuffer(ProcPath.AUXV);
        HashMap<Integer, Long> auxvMap = new HashMap<Integer, Long>();
        do {
            if ((key = FileUtil.readNativeLongFromBuffer(buff).intValue()) <= 0) continue;
            auxvMap.put(key, FileUtil.readNativeLongFromBuffer(buff).longValue());
        } while (key > 0);
        return auxvMap;
    }
}

