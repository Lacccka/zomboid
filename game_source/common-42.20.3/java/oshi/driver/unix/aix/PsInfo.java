/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.aix;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.LibCAPI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.unix.AixLibc;
import oshi.util.FileUtil;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
public final class PsInfo {
    private static final Logger LOG = LoggerFactory.getLogger(PsInfo.class);
    private static final AixLibc LIBC = AixLibc.INSTANCE;
    private static final long PAGE_SIZE = 4096L;

    private PsInfo() {
    }

    public static AixLibc.AixPsInfo queryPsInfo(int pid) {
        return new AixLibc.AixPsInfo(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/psinfo", pid)));
    }

    public static AixLibc.AixLwpsInfo queryLwpsInfo(int pid, int tid) {
        return new AixLibc.AixLwpsInfo(FileUtil.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/lwpsinfo", pid, tid)));
    }

    public static Triplet<Integer, Long, Long> queryArgsEnvAddrs(int pid, AixLibc.AixPsInfo psinfo) {
        if (psinfo != null) {
            int argc = psinfo.pr_argc;
            if (argc > 0) {
                long argv = psinfo.pr_argv;
                long envp = psinfo.pr_envp;
                return new Triplet<Integer, Long, Long>(argc, argv, envp);
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
    public static Pair<List<String>, Map<String, String>> queryArgsEnv(int pid, AixLibc.AixPsInfo psinfo) {
        ArrayList<String> args2 = new ArrayList<String>();
        LinkedHashMap<String, String> env = new LinkedHashMap<String, String>();
        Triplet<Integer, Long, Long> addrs = PsInfo.queryArgsEnvAddrs(pid, psinfo);
        if (addrs != null) {
            String procas = "/proc/" + pid + "/as";
            int fd = LIBC.open(procas, 0);
            if (fd < 0) {
                LOG.trace("No permission to read file: {} ", (Object)procas);
                return new Pair<List<String>, Map<String, String>>(args2, env);
            }
            try {
                long increment;
                int argc = addrs.getA();
                long argv = addrs.getB();
                long envp = addrs.getC();
                Path p = Paths.get("/proc/" + pid + "/status", new String[0]);
                try {
                    byte[] status = Files.readAllBytes(p);
                    increment = status[17] == 1 ? 8L : 4L;
                }
                catch (IOException e) {
                    Pair<List<String>, Map<String, String>> pair = new Pair<List<String>, Map<String, String>>(args2, env);
                    LIBC.close(fd);
                    return pair;
                }
                try (Memory buffer = new Memory(8192L);){
                    long argp;
                    LibCAPI.size_t bufSize = new LibCAPI.size_t(buffer.size());
                    long bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, 0L, argv);
                    long[] argPtr = new long[argc];
                    long l = argp = bufStart == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, argv - bufStart, increment);
                    if (argp > 0L) {
                        for (int i = 0; i < argc; ++i) {
                            long offset = argp + (long)i * increment;
                            argPtr[i] = (bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset)) == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        }
                    }
                    bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, envp);
                    ArrayList<Long> envPtrList = new ArrayList<Long>();
                    long addr = bufStart == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, envp - bufStart, increment);
                    int limit = 500;
                    long offset = addr;
                    while (addr != 0L && --limit > 0) {
                        long envPtr;
                        long l2 = envPtr = (bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset)) == 0L ? 0L : PsInfo.getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        if (envPtr != 0L) {
                            envPtrList.add(envPtr);
                        }
                        offset += increment;
                    }
                    for (int i = 0; i < argPtr.length && argPtr[i] != 0L; ++i) {
                        String argStr;
                        if ((bufStart = PsInfo.conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, argPtr[i])) == 0L || (argStr = buffer.getString(argPtr[i] - bufStart)).isEmpty()) continue;
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
        if (addr < bufStart || addr - bufStart > 4096L) {
            long newStart = Math.floorDiv(addr, 4096L) * 4096L;
            LibCAPI.ssize_t result = LIBC.pread(fd, buffer, bufSize, new NativeLong(newStart));
            if (result.longValue() < 4096L) {
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

