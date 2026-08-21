/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.platform.win32.WinBase;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.Immutable;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.ProcessInformation;
import oshi.driver.windows.registry.HkeyPerformanceDataUtil;
import oshi.util.GlobalConfig;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class ProcessPerformanceData {
    private static final String PROCESS = "Process";
    private static final boolean PERFDATA = GlobalConfig.get("oshi.os.windows.hkeyperfdata", true);

    private ProcessPerformanceData() {
    }

    public static Map<Integer, PerfCounterBlock> buildProcessMapFromRegistry(Collection<Integer> pids) {
        Triplet<List<Map<ProcessInformation.ProcessPerformanceProperty, Object>>, Long, Long> processData = null;
        if (PERFDATA) {
            processData = HkeyPerformanceDataUtil.readPerfDataFromRegistry(PROCESS, ProcessInformation.ProcessPerformanceProperty.class);
        }
        if (processData == null) {
            return null;
        }
        List<Map<ProcessInformation.ProcessPerformanceProperty, Object>> processInstanceMaps = processData.getA();
        long now = processData.getC();
        HashMap<Integer, PerfCounterBlock> processMap = new HashMap<Integer, PerfCounterBlock>();
        for (Map<ProcessInformation.ProcessPerformanceProperty, Object> processInstanceMap : processInstanceMaps) {
            long upTime;
            int pid = (Integer)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.IDPROCESS);
            String name = (String)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.NAME);
            if (pids != null && !pids.contains(pid) || "_Total".equals(name)) continue;
            long ctime = (Long)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.ELAPSEDTIME);
            if (ctime > now) {
                ctime = WinBase.FILETIME.filetimeToDate((int)(ctime >> 32), (int)(ctime & 0xFFFFFFFFL)).getTime();
            }
            if ((upTime = now - ctime) < 1L) {
                upTime = 1L;
            }
            processMap.put(pid, new PerfCounterBlock(name, (Integer)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.CREATINGPROCESSID), (Integer)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.PRIORITYBASE), (Long)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.PRIVATEBYTES), ctime, upTime, (Long)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.IOREADBYTESPERSEC), (Long)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.IOWRITEBYTESPERSEC), (Integer)processInstanceMap.get(ProcessInformation.ProcessPerformanceProperty.PAGEFAULTSPERSEC)));
        }
        return processMap;
    }

    public static Map<Integer, PerfCounterBlock> buildProcessMapFromPerfCounters(Collection<Integer> pids) {
        return ProcessPerformanceData.buildProcessMapFromPerfCounters(pids, null);
    }

    public static Map<Integer, PerfCounterBlock> buildProcessMapFromPerfCounters(Collection<Integer> pids, String procName) {
        HashMap<Integer, PerfCounterBlock> processMap = new HashMap<Integer, PerfCounterBlock>();
        Pair<List<String>, Map<ProcessInformation.ProcessPerformanceProperty, List<Long>>> instanceValues = ProcessInformation.queryProcessCounters();
        long now = System.currentTimeMillis();
        List<String> instances = instanceValues.getA();
        Map<ProcessInformation.ProcessPerformanceProperty, List<Long>> valueMap = instanceValues.getB();
        List<Long> pidList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.IDPROCESS);
        List<Long> ppidList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.CREATINGPROCESSID);
        List<Long> priorityList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.PRIORITYBASE);
        List<Long> ioReadList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.IOREADBYTESPERSEC);
        List<Long> ioWriteList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.IOWRITEBYTESPERSEC);
        List<Long> workingSetSizeList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.PRIVATEBYTES);
        List<Long> elapsedTimeList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.ELAPSEDTIME);
        List<Long> pageFaultsList = valueMap.get(ProcessInformation.ProcessPerformanceProperty.PAGEFAULTSPERSEC);
        for (int inst = 0; inst < instances.size(); ++inst) {
            long upTime;
            int pid = pidList.get(inst).intValue();
            if (pids != null && !pids.contains(pid)) continue;
            long ctime = elapsedTimeList.get(inst);
            if (ctime > now) {
                ctime = WinBase.FILETIME.filetimeToDate((int)(ctime >> 32), (int)(ctime & 0xFFFFFFFFL)).getTime();
            }
            if ((upTime = now - ctime) < 1L) {
                upTime = 1L;
            }
            processMap.put(pid, new PerfCounterBlock(instances.get(inst), ppidList.get(inst).intValue(), priorityList.get(inst).intValue(), workingSetSizeList.get(inst), ctime, upTime, ioReadList.get(inst), ioWriteList.get(inst), pageFaultsList.get(inst).intValue()));
        }
        return processMap;
    }

    @Immutable
    public static class PerfCounterBlock {
        private final String name;
        private final int parentProcessID;
        private final int priority;
        private final long residentSetSize;
        private final long startTime;
        private final long upTime;
        private final long bytesRead;
        private final long bytesWritten;
        private final int pageFaults;

        public PerfCounterBlock(String name, int parentProcessID, int priority, long residentSetSize, long startTime, long upTime, long bytesRead, long bytesWritten, int pageFaults) {
            this.name = name;
            this.parentProcessID = parentProcessID;
            this.priority = priority;
            this.residentSetSize = residentSetSize;
            this.startTime = startTime;
            this.upTime = upTime;
            this.bytesRead = bytesRead;
            this.bytesWritten = bytesWritten;
            this.pageFaults = pageFaults;
        }

        public String getName() {
            return this.name;
        }

        public int getParentProcessID() {
            return this.parentProcessID;
        }

        public int getPriority() {
            return this.priority;
        }

        public long getResidentSetSize() {
            return this.residentSetSize;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public long getUpTime() {
            return this.upTime;
        }

        public long getBytesRead() {
            return this.bytesRead;
        }

        public long getBytesWritten() {
            return this.bytesWritten;
        }

        public long getPageFaults() {
            return this.pageFaults;
        }
    }
}

