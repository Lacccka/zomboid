/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinPerf;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.SuppressForbidden;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.util.platform.windows.PerfCounterWildcardQuery;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class HkeyPerformanceDataUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HkeyPerformanceDataUtil.class);
    private static final String HKEY_PERFORMANCE_TEXT = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Perflib\\009";
    private static final String COUNTER = "Counter";
    private static final Map<String, Integer> COUNTER_INDEX_MAP = HkeyPerformanceDataUtil.mapCounterIndicesFromRegistry();
    private static int maxPerfBufferSize = 16384;

    private HkeyPerformanceDataUtil() {
    }

    public static <T extends Enum<T>> Triplet<List<Map<T, Object>>, Long, Long> readPerfDataFromRegistry(String objectName, Class<T> counterEnum) {
        Pair<Integer, EnumMap<T, Integer>> indices = HkeyPerformanceDataUtil.getCounterIndices(objectName, counterEnum);
        if (indices == null) {
            return null;
        }
        try (Memory pPerfData = HkeyPerformanceDataUtil.readPerfDataBuffer(objectName);){
            if (pPerfData == null) {
                Triplet<List<Map<T, Object>>, Long, Long> triplet = null;
                return triplet;
            }
            WinPerf.PERF_DATA_BLOCK perfData = new WinPerf.PERF_DATA_BLOCK(pPerfData.share(0L));
            long perfTime100nSec = perfData.PerfTime100nSec.getValue();
            long now = WinBase.FILETIME.filetimeToDate((int)(perfTime100nSec >> 32), (int)(perfTime100nSec & 0xFFFFFFFFL)).getTime();
            long perfObjectOffset = perfData.HeaderLength;
            for (int obj = 0; obj < perfData.NumObjectTypes; ++obj) {
                WinPerf.PERF_OBJECT_TYPE perfObject = new WinPerf.PERF_OBJECT_TYPE(pPerfData.share(perfObjectOffset));
                if (perfObject.ObjectNameTitleIndex == COUNTER_INDEX_MAP.get(objectName)) {
                    long perfCounterOffset = perfObjectOffset + (long)perfObject.HeaderLength;
                    HashMap<Integer, Integer> counterOffsetMap = new HashMap<Integer, Integer>();
                    HashMap<Integer, Integer> counterSizeMap = new HashMap<Integer, Integer>();
                    for (int counter = 0; counter < perfObject.NumCounters; ++counter) {
                        WinPerf.PERF_COUNTER_DEFINITION perfCounter = new WinPerf.PERF_COUNTER_DEFINITION(pPerfData.share(perfCounterOffset));
                        counterOffsetMap.put(perfCounter.CounterNameTitleIndex, perfCounter.CounterOffset);
                        counterSizeMap.put(perfCounter.CounterNameTitleIndex, perfCounter.CounterSize);
                        perfCounterOffset += (long)perfCounter.ByteLength;
                    }
                    long perfInstanceOffset = perfObjectOffset + (long)perfObject.DefinitionLength;
                    ArrayList counterMaps = new ArrayList(perfObject.NumInstances);
                    for (int inst = 0; inst < perfObject.NumInstances; ++inst) {
                        WinPerf.PERF_INSTANCE_DEFINITION perfInstance = new WinPerf.PERF_INSTANCE_DEFINITION(pPerfData.share(perfInstanceOffset));
                        long perfCounterBlockOffset = perfInstanceOffset + (long)perfInstance.ByteLength;
                        EnumMap<T, Object> counterMap = new EnumMap<T, Object>(counterEnum);
                        Enum[] counterKeys = (Enum[])counterEnum.getEnumConstants();
                        counterMap.put((T)counterKeys[0], (Object)pPerfData.getWideString(perfInstanceOffset + (long)perfInstance.NameOffset));
                        for (int i = 1; i < counterKeys.length; ++i) {
                            Enum key = counterKeys[i];
                            int keyIndex = COUNTER_INDEX_MAP.get(((PerfCounterWildcardQuery.PdhCounterWildcardProperty)((Object)key)).getCounter());
                            int size = counterSizeMap.getOrDefault(keyIndex, 0);
                            if (size == 4) {
                                counterMap.put((T)key, (Object)pPerfData.getInt(perfCounterBlockOffset + (long)((Integer)counterOffsetMap.get(keyIndex)).intValue()));
                                continue;
                            }
                            if (size == 8) {
                                counterMap.put((T)key, (Object)pPerfData.getLong(perfCounterBlockOffset + (long)((Integer)counterOffsetMap.get(keyIndex)).intValue()));
                                continue;
                            }
                            Triplet<List<Map<T, Object>>, Long, Long> triplet = null;
                            return triplet;
                        }
                        counterMaps.add(counterMap);
                        perfInstanceOffset = perfCounterBlockOffset + (long)new WinPerf.PERF_COUNTER_BLOCK((Pointer)pPerfData.share((long)perfCounterBlockOffset)).ByteLength;
                    }
                    Triplet<List<Map<T, Object>>, Long, Long> triplet = new Triplet<List<Map<T, Object>>, Long, Long>(counterMaps, perfTime100nSec, now);
                    return triplet;
                }
                perfObjectOffset += (long)perfObject.TotalByteLength;
            }
        }
        return null;
    }

    private static <T extends Enum<T>> Pair<Integer, EnumMap<T, Integer>> getCounterIndices(String objectName, Class<T> counterEnum) {
        if (!COUNTER_INDEX_MAP.containsKey(objectName)) {
            LOG.debug("Couldn't find counter index of {}.", (Object)objectName);
            return null;
        }
        int counterIndex = COUNTER_INDEX_MAP.get(objectName);
        Enum[] enumConstants = (Enum[])counterEnum.getEnumConstants();
        EnumMap<T, Integer> indexMap = new EnumMap<T, Integer>(counterEnum);
        for (int i = 1; i < enumConstants.length; ++i) {
            Enum key = enumConstants[i];
            String counterName = ((PerfCounterWildcardQuery.PdhCounterWildcardProperty)((Object)key)).getCounter();
            if (!COUNTER_INDEX_MAP.containsKey(counterName)) {
                LOG.debug("Couldn't find counter index of {}.", (Object)counterName);
                return null;
            }
            indexMap.put(key, COUNTER_INDEX_MAP.get(counterName));
        }
        return new Pair<Integer, EnumMap<T, Integer>>(counterIndex, indexMap);
    }

    private static synchronized Memory readPerfDataBuffer(String objectName) {
        String objectIndexStr = Integer.toString(COUNTER_INDEX_MAP.get(objectName));
        try (ByRef.CloseableIntByReference lpcbData = new ByRef.CloseableIntByReference(maxPerfBufferSize);){
            Memory pPerfData = new Memory(maxPerfBufferSize);
            int ret = Advapi32.INSTANCE.RegQueryValueEx(WinReg.HKEY_PERFORMANCE_DATA, objectIndexStr, 0, null, pPerfData, (IntByReference)lpcbData);
            if (ret != 0 && ret != 234) {
                LOG.error("Error reading performance data from registry for {}.", (Object)objectName);
                pPerfData.close();
                Memory memory = null;
                return memory;
            }
            while (ret == 234) {
                lpcbData.setValue(maxPerfBufferSize += 8192);
                pPerfData.close();
                pPerfData = new Memory(maxPerfBufferSize);
                ret = Advapi32.INSTANCE.RegQueryValueEx(WinReg.HKEY_PERFORMANCE_DATA, objectIndexStr, 0, null, pPerfData, (IntByReference)lpcbData);
            }
            Memory memory = pPerfData;
            return memory;
        }
    }

    @SuppressForbidden(reason="Catching the error here")
    private static Map<String, Integer> mapCounterIndicesFromRegistry() {
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
        try {
            String[] counterText = Advapi32Util.registryGetStringArray(WinReg.HKEY_LOCAL_MACHINE, HKEY_PERFORMANCE_TEXT, COUNTER);
            for (int i = 1; i < counterText.length; i += 2) {
                indexMap.putIfAbsent(counterText[i], Integer.parseInt(counterText[i - 1]));
            }
        }
        catch (Win32Exception we) {
            LOG.error("Unable to locate English counter names in registry Perflib 009. Counters may need to be rebuilt: ", we);
        }
        catch (NumberFormatException nfe) {
            LOG.error("Unable to parse English counter names in registry Perflib 009.");
        }
        return Collections.unmodifiableMap(indexMap);
    }
}

