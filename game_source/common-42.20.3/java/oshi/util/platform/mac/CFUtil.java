/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class CFUtil {
    private CFUtil() {
    }

    public static String cfPointerToString(Pointer result) {
        return CFUtil.cfPointerToString(result, true);
    }

    public static String cfPointerToString(Pointer result, boolean returnUnknown) {
        String s = "";
        if (result != null) {
            CoreFoundation.CFStringRef cfs = new CoreFoundation.CFStringRef(result);
            s = cfs.stringValue();
        }
        if (returnUnknown && s.isEmpty()) {
            return "unknown";
        }
        return s;
    }
}

