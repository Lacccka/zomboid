/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.OSProcess;
import oshi.util.Constants;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class ProcessStat {
    private static final Pattern SOCKET = Pattern.compile("socket:\\[(\\d+)\\]");
    public static final int PROC_PID_STAT_LENGTH;

    private ProcessStat() {
    }

    public static Triplet<String, Character, Map<PidStat, Long>> getPidStats(int pid) {
        String stat = FileUtil.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STAT, pid));
        if (stat.isEmpty()) {
            return null;
        }
        int nameStart = stat.indexOf(40) + 1;
        int nameEnd = stat.indexOf(41);
        String name = stat.substring(nameStart, nameEnd);
        Character state = Character.valueOf(stat.charAt(nameEnd + 2));
        String[] split = ParseUtil.whitespaces.split(stat.substring(nameEnd + 4).trim());
        EnumMap<PidStat, Long> statMap = new EnumMap<PidStat, Long>(PidStat.class);
        PidStat[] enumArray = (PidStat[])PidStat.class.getEnumConstants();
        for (int i = 3; i < enumArray.length && i - 3 < split.length; ++i) {
            statMap.put(enumArray[i], ParseUtil.parseLongOrDefault(split[i - 3], 0L));
        }
        return new Triplet<String, Character, Map<PidStat, Long>>(name, state, statMap);
    }

    public static Map<PidStatM, Long> getPidStatM(int pid) {
        String statm = FileUtil.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STATM, pid));
        if (statm.isEmpty()) {
            return null;
        }
        String[] split = ParseUtil.whitespaces.split(statm);
        EnumMap<PidStatM, Long> statmMap = new EnumMap<PidStatM, Long>(PidStatM.class);
        PidStatM[] enumArray = (PidStatM[])PidStatM.class.getEnumConstants();
        for (int i = 0; i < enumArray.length && i < split.length; ++i) {
            statmMap.put(enumArray[i], ParseUtil.parseLongOrDefault(split[i], 0L));
        }
        return statmMap;
    }

    public static File[] getFileDescriptorFiles(int pid) {
        return ProcessStat.listNumericFiles(String.format(Locale.ROOT, ProcPath.PID_FD, pid));
    }

    public static File[] getPidFiles() {
        return ProcessStat.listNumericFiles(ProcPath.PROC);
    }

    public static Map<Long, Integer> querySocketToPidMap() {
        HashMap<Long, Integer> pidMap = new HashMap<Long, Integer>();
        for (File f : ProcessStat.getPidFiles()) {
            File[] fds;
            int pid = ParseUtil.parseIntOrDefault(f.getName(), -1);
            for (File fd : fds = ProcessStat.getFileDescriptorFiles(pid)) {
                Matcher m;
                String symLink = FileUtil.readSymlinkTarget(fd);
                if (symLink == null || !(m = SOCKET.matcher(symLink)).matches()) continue;
                pidMap.put(ParseUtil.parseLongOrDefault(m.group(1), -1L), pid);
            }
        }
        return pidMap;
    }

    public static List<Integer> getThreadIds(int pid) {
        File[] threads = ProcessStat.listNumericFiles(String.format(Locale.ROOT, ProcPath.TASK_PATH, pid));
        return Arrays.stream(threads).map(thread2 -> ParseUtil.parseIntOrDefault(thread2.getName(), 0)).filter(threadId -> threadId != pid).collect(Collectors.toList());
    }

    private static File[] listNumericFiles(String path) {
        File directory = new File(path);
        File[] numericFiles = directory.listFiles(file -> Constants.DIGITS.matcher(file.getName()).matches());
        return numericFiles == null ? new File[]{} : numericFiles;
    }

    public static OSProcess.State getState(char stateValue) {
        return switch (stateValue) {
            case 'R' -> OSProcess.State.RUNNING;
            case 'S' -> OSProcess.State.SLEEPING;
            case 'D' -> OSProcess.State.WAITING;
            case 'Z' -> OSProcess.State.ZOMBIE;
            case 'T' -> OSProcess.State.STOPPED;
            default -> OSProcess.State.OTHER;
        };
    }

    static {
        String stat = FileUtil.getStringFromFile(ProcPath.SELF_STAT);
        PROC_PID_STAT_LENGTH = stat.contains(")") ? ParseUtil.countStringToLongArray(stat, ' ') + 3 : 52;
    }

    public static enum PidStat {
        PID,
        COMM,
        STATE,
        PPID,
        PGRP,
        SESSION,
        TTY_NR,
        PTGID,
        FLAGS,
        MINFLT,
        CMINFLT,
        MAJFLT,
        CMAJFLT,
        UTIME,
        STIME,
        CUTIME,
        CSTIME,
        PRIORITY,
        NICE,
        NUM_THREADS,
        ITREALVALUE,
        STARTTIME,
        VSIZE,
        RSS,
        RSSLIM,
        STARTCODE,
        ENDCODE,
        STARTSTACK,
        KSTKESP,
        KSTKEIP,
        SIGNAL,
        BLOCKED,
        SIGIGNORE,
        SIGCATCH,
        WCHAN,
        NSWAP,
        CNSWAP,
        EXIT_SIGNAL,
        PROCESSOR,
        RT_PRIORITY,
        POLICY,
        DELAYACCT_BLKIO_TICKS,
        GUEST_TIME,
        CGUEST_TIME,
        START_DATA,
        END_DATA,
        START_BRK,
        ARG_START,
        ARG_END,
        ENV_START,
        ENV_END,
        EXIT_CODE;

    }

    public static enum PidStatM {
        SIZE,
        RESIDENT,
        SHARED,
        TEXT,
        LIB,
        DATA,
        DT;

    }
}

