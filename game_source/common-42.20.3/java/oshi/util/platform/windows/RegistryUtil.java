/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.windows;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.ParseUtil;

public final class RegistryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RegistryUtil.class);
    private static final long THIRTY_YEARS_IN_SECS = 946080000L;
    private static final Advapi32 ADV = Advapi32.INSTANCE;

    private RegistryUtil() {
    }

    public static long getLongValue(WinReg.HKEY root, String path, String key) {
        try {
            Object val = Advapi32Util.registryGetValue(root, path, key);
            return RegistryUtil.registryValueToLong(val);
        }
        catch (Win32Exception e) {
            LOG.trace("Unable to access " + path + ": " + e.getMessage());
            return 0L;
        }
    }

    public static long getLongValue(WinReg.HKEY root, String path, String key, int accessFlag) {
        Object val = RegistryUtil.getRegistryValueOrNull(root, path, key, accessFlag);
        return RegistryUtil.registryValueToLong(val);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object getRegistryValueOrNull(WinReg.HKEY root, String path, String key, int accessFlag) {
        int rc;
        Object object;
        WinReg.HKEY hKey = null;
        try {
            hKey = Advapi32Util.registryGetKey(root, path, 0x20019 | accessFlag).getValue();
            Object value = Advapi32Util.registryGetValue(root, path, key);
            Object object2 = object = Objects.isNull(value) ? null : value;
        }
        catch (Win32Exception e) {
            int rc2;
            try {
                LOG.trace("Unable to access " + path + " with flag " + accessFlag + ": " + e.getMessage());
                if (hKey == null || (rc2 = ADV.RegCloseKey(hKey)) == 0) return null;
            }
            catch (Throwable throwable) {
                int rc3;
                if (hKey == null || (rc3 = ADV.RegCloseKey(hKey)) == 0) throw throwable;
                throw new Win32Exception(rc3);
            }
            throw new Win32Exception(rc2);
        }
        if (hKey == null || (rc = ADV.RegCloseKey(hKey)) == 0) return object;
        throw new Win32Exception(rc);
    }

    private static long registryValueToLong(Object val) {
        if (val == null) {
            return 0L;
        }
        long currentTimeSecs = System.currentTimeMillis() / 1000L;
        long minSaneTimestamp = currentTimeSecs - 946080000L;
        if (val instanceof Integer) {
            int value = (Integer)val;
            if ((long)value > minSaneTimestamp && (long)value < currentTimeSecs) {
                return (long)value * 1000L;
            }
            return value;
        }
        if (val instanceof String) {
            String dateStr = ((String)val).trim();
            long epoch = ParseUtil.parseDateToEpoch(dateStr, "yyyyMMdd");
            if (epoch == 0L) {
                epoch = ParseUtil.parseDateToEpoch(dateStr, "MM/dd/yyyy");
            }
            return epoch;
        }
        return 0L;
    }

    public static String getStringValue(WinReg.HKEY root, String path, String key) {
        try {
            Object val = Advapi32Util.registryGetValue(root, path, key);
            return RegistryUtil.registryValueToString(val);
        }
        catch (Win32Exception e) {
            LOG.trace("Unable to access " + path + ": " + e.getMessage());
            return null;
        }
    }

    public static String getStringValue(WinReg.HKEY root, String path, String key, int accessFlag) {
        Object val = RegistryUtil.getRegistryValueOrNull(root, path, key, accessFlag);
        return RegistryUtil.registryValueToString(val);
    }

    private static String registryValueToString(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return ((String)val).trim();
        }
        if (val instanceof byte[]) {
            return ParseUtil.decodeBinaryToString((byte[])val);
        }
        return null;
    }
}

