/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.mac.IOKit;
import oshi.jna.platform.mac.SystemB;
import oshi.util.ParseUtil;

@ThreadSafe
public final class SmcUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SmcUtil.class);
    private static final IOKit IO = IOKit.INSTANCE;
    private static Map<Integer, IOKit.SMCKeyDataKeyInfo> keyInfoCache = new ConcurrentHashMap<Integer, IOKit.SMCKeyDataKeyInfo>();
    private static final byte[] DATATYPE_SP78 = ParseUtil.asciiStringToByteArray("sp78", 5);
    private static final byte[] DATATYPE_FPE2 = ParseUtil.asciiStringToByteArray("fpe2", 5);
    private static final byte[] DATATYPE_FLT = ParseUtil.asciiStringToByteArray("flt ", 5);
    public static final String SMC_KEY_FAN_NUM = "FNum";
    public static final String SMC_KEY_FAN_SPEED = "F%dAc";
    public static final String SMC_KEY_CPU_TEMP = "TC0P";
    public static final String SMC_KEY_CPU_VOLTAGE = "VC0C";
    public static final byte SMC_CMD_READ_BYTES = 5;
    public static final byte SMC_CMD_READ_KEYINFO = 9;
    public static final int KERNEL_INDEX_SMC = 2;

    private SmcUtil() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static IOKit.IOConnect smcOpen() {
        IOKit.IOService smcService = IOKitUtil.getMatchingService("AppleSMC");
        if (smcService != null) {
            try (ByRef.CloseablePointerByReference connPtr = new ByRef.CloseablePointerByReference();){
                int result = IO.IOServiceOpen(smcService, SystemB.INSTANCE.mach_task_self(), 0, connPtr);
                if (result == 0) {
                    IOKit.IOConnect iOConnect = new IOKit.IOConnect(connPtr.getValue());
                    return iOConnect;
                }
                if (!LOG.isErrorEnabled()) return null;
                LOG.error(String.format(Locale.ROOT, "Unable to open connection to AppleSMC service. Error: 0x%08x", result));
                return null;
            }
            finally {
                smcService.release();
            }
        } else {
            LOG.error("Unable to locate AppleSMC service");
        }
        return null;
    }

    public static int smcClose(IOKit.IOConnect conn) {
        return IO.IOServiceClose(conn);
    }

    public static double smcGetFloat(IOKit.IOConnect conn, String key) {
        try (IOKit.SMCVal val = new IOKit.SMCVal();){
            int result = SmcUtil.smcReadKey(conn, key, val);
            if (result == 0 && val.dataSize > 0) {
                if (Arrays.equals(val.dataType, DATATYPE_SP78) && val.dataSize == 2) {
                    double d = (double)val.bytes[0] + (double)val.bytes[1] / 256.0;
                    return d;
                }
                if (Arrays.equals(val.dataType, DATATYPE_FPE2) && val.dataSize == 2) {
                    double d = ParseUtil.byteArrayToFloat(val.bytes, val.dataSize, 2);
                    return d;
                }
                if (Arrays.equals(val.dataType, DATATYPE_FLT) && val.dataSize == 4) {
                    double d = ByteBuffer.wrap(val.bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    return d;
                }
            }
        }
        return 0.0;
    }

    public static long smcGetLong(IOKit.IOConnect conn, String key) {
        try (IOKit.SMCVal val = new IOKit.SMCVal();){
            int result = SmcUtil.smcReadKey(conn, key, val);
            if (result == 0) {
                long l = ParseUtil.byteArrayToLong(val.bytes, val.dataSize);
                return l;
            }
        }
        return 0L;
    }

    public static int smcGetKeyInfo(IOKit.IOConnect conn, IOKit.SMCKeyData inputStructure, IOKit.SMCKeyData outputStructure) {
        if (keyInfoCache.containsKey(inputStructure.key)) {
            IOKit.SMCKeyDataKeyInfo keyInfo = keyInfoCache.get(inputStructure.key);
            outputStructure.keyInfo.dataSize = keyInfo.dataSize;
            outputStructure.keyInfo.dataType = keyInfo.dataType;
            outputStructure.keyInfo.dataAttributes = keyInfo.dataAttributes;
        } else {
            inputStructure.data8 = (byte)9;
            int result = SmcUtil.smcCall(conn, 2, inputStructure, outputStructure);
            if (result != 0) {
                return result;
            }
            IOKit.SMCKeyDataKeyInfo keyInfo = new IOKit.SMCKeyDataKeyInfo();
            keyInfo.dataSize = outputStructure.keyInfo.dataSize;
            keyInfo.dataType = outputStructure.keyInfo.dataType;
            keyInfo.dataAttributes = outputStructure.keyInfo.dataAttributes;
            keyInfoCache.put(inputStructure.key, keyInfo);
        }
        return 0;
    }

    public static int smcReadKey(IOKit.IOConnect conn, String key, IOKit.SMCVal val) {
        try (IOKit.SMCKeyData inputStructure = new IOKit.SMCKeyData();){
            int result;
            IOKit.SMCKeyData outputStructure;
            block12: {
                outputStructure = new IOKit.SMCKeyData();
                try {
                    inputStructure.key = (int)ParseUtil.strToLong(key, 4);
                    result = SmcUtil.smcGetKeyInfo(conn, inputStructure, outputStructure);
                    if (result != 0) break block12;
                    val.dataSize = outputStructure.keyInfo.dataSize;
                    val.dataType = ParseUtil.longToByteArray(outputStructure.keyInfo.dataType, 4, 5);
                    inputStructure.keyInfo.dataSize = val.dataSize;
                    inputStructure.data8 = (byte)5;
                    result = SmcUtil.smcCall(conn, 2, inputStructure, outputStructure);
                    if (result != 0) break block12;
                    System.arraycopy(outputStructure.bytes, 0, val.bytes, 0, val.bytes.length);
                    int n = 0;
                    outputStructure.close();
                    return n;
                }
                catch (Throwable throwable) {
                    try {
                        outputStructure.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            int n = result;
            outputStructure.close();
            return n;
        }
    }

    public static int smcCall(IOKit.IOConnect conn, int index, IOKit.SMCKeyData inputStructure, IOKit.SMCKeyData outputStructure) {
        try (ByRef.CloseableNativeLongByReference size = new ByRef.CloseableNativeLongByReference(new NativeLong((long)outputStructure.size()));){
            int n = IO.IOConnectCallStructMethod(conn, index, inputStructure, new NativeLong((long)inputStructure.size()), outputStructure, size);
            return n;
        }
    }
}

