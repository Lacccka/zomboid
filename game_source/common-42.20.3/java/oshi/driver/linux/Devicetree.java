/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class Devicetree {
    private Devicetree() {
    }

    public static String queryModel() {
        String modelStr = FileUtil.getStringFromFile(SysPath.MODEL);
        if (!modelStr.isEmpty()) {
            return modelStr.replace("Machine: ", "");
        }
        return null;
    }
}

