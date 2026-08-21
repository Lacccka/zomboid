/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.solaris;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.unix.SolarisLibc;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Quartet;

@ThreadSafe
public final class PsInfo {
    private static final Logger LOG = LoggerFactory.getLogger(PsInfo.class);
    private static final SolarisLibc LIBC = SolarisLibc.INSTANCE;
    private static final long PAGE_SIZE = ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("pagesize"), 4096L);

    private PsInfo() {
    }

    public static SolarisLibc.SolarisPsInfo queryPsInfo(int pid) {
        return new SolarisLibc.SolarisPsInfo(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/psinfo", pid)));
    }

    public static SolarisLibc.SolarisLwpsInfo queryLwpsInfo(int pid, int tid) {
        return new SolarisLibc.SolarisLwpsInfo(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/lwpsinfo", pid, tid)));
    }

    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid) {
        return new SolarisLibc.SolarisPrUsage(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/usage", pid)));
    }

    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid, int tid) {
        return new SolarisLibc.SolarisPrUsage(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/usage", pid, tid)));
    }

    public static Quartet<Integer, Long, Long, Byte> queryArgsEnvAddrs(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        if (psinfo != null) {
            int argc = psinfo.pr_argc;
            if (argc > 0) {
                long argv = Pointer.nativeValue(psinfo.pr_argv);
                byte dmodel = psinfo.pr_dmodel;
                long envp = Pointer.nativeValue(psinfo.pr_envp);
                if ((long)(dmodel * 4) == (envp - argv) / (long)(argc + 1)) {
                    return new Quartet<Integer, Long, Long, Byte>(argc, argv, envp, dmodel);
                }
                LOG.trace("Failed data model and offset increment sanity check: dm={} diff={}", (Object)dmodel, (Object)(envp - argv));
                return null;
            }
            LOG.trace("Failed argc sanity check: argc={}", (Object)argc);
            return null;
        }
        LOG.trace("Failed to read psinfo file for pid: {} ", (Object)pid);
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Pair<List<String>, Map<String, String>> queryArgsEnv(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        ArrayList<String> args2 = new ArrayList<String>();
        LinkedHashMap<String, String> env = new LinkedHashMap<String, String>();
        Quartet<Integer, Long, Long, Byte> addrs = PsInfo.queryArgsEnvAddrs(pid, psinfo);
        if (addrs != null) {
            String procas = "/proc/" + pid + "/as";
            int fd = LIBC.open(procas, 0);
            if (fd < 0) {
                LOG.trace("No permission to read file: {} ", (Object)procas);
                return new Pair<List<String>, Map<String, String>>(args2, env);
            }
            try {
                int argc = addrs.getA();
                long argv = addrs.getB();
                long envp = addrs.getC();
                long increment = (long)addrs.getD().byteValue() * 4L;
                long bufStart = 0L;
                try (Memory buffer = new Memory(PAGE_SIZE * 2L);){
                    LibCAPI.size_t bufSize = new LibCAPI.size_t(buffer.size());
                    long[] argp = new long[argc];
                    long offset = argv;
                    for (int i = 0; i < argc; ++i) {
                        argp[i] = (bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset)) == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        offset += increment;
                    }
                    ArrayList<Long> envPtrList = new ArrayList<Long>();
                    offset = envp;
                    long addr = 0L;
                    int limit = 500;
                    do {
                        long l = addr = (bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset)) == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        if (addr != 0L) {
                            envPtrList.add(addr);
                        }
                        offset += increment;
                    } while (addr != 0L && --limit > 0);
                    for (int i = 0; i < argp.length && argp[i] != 0L; ++i) {
                        String argStr;
                        if ((bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, argp[i])) == 0L || (argStr = buffer.getString(argp[i] - bufStart)).isEmpty()) continue;
                        args2.add(argStr);
                    }
                    for (Long envPtr : envPtrList) {
                        String envStr;
                        int idx;
                        if ((bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, envPtr)) == 0L || (idx = (envStr = buffer.getString(envPtr - bufStart)).indexOf(61)) <= 0) continue;
                        env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                    }
                }
            }
            finally {
                LIBC.close(fd);
            }
        }
        return new Pair<List<String>, Map<String, String>>(args2, env);
    }

    private static long conditionallyReadBufferFromStartOfPage(int fd, Memory buffer, LibCAPI.size_t bufSize, long bufStart, long addr) {
        if (addr < bufStart || addr - bufStart > PAGE_SIZE) {
            long newStart = Math.floorDiv(addr, PAGE_SIZE) * PAGE_SIZE;
            LibCAPI.ssize_t result = LIBC.pread(fd, buffer, bufSize, new NativeLong(newStart));
            if (result.longValue() < PAGE_SIZE) {
                LOG.debug("Failed to read page from address space: {} bytes read", (Object)result.longValue());
                return 0L;
            }
            return newStart;
        }
        return bufStart;
    }

    private static long getOffsetFromBuffer(Memory buffer, long offset, long increment) {
        return increment == 8L ? buffer.getLong(offset) : (long)buffer.getInt(offset);
    }
}

