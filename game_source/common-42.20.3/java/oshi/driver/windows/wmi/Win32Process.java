/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32Process {
    private static final String WIN32_PROCESS = "Win32_Process";

    private Win32Process() {
    }

    public static WbemcliUtil.WmiResult<CommandLineProperty> queryCommandLines(Set<Integer> pidsToQuery) {
        Object sb = WIN32_PROCESS;
        if (pidsToQuery != null) {
            sb = (String)sb + " WHERE ProcessID=" + pidsToQuery.stream().map(String::valueOf).collect(Collectors.joining(" OR PROCESSID="));
        }
        WbemcliUtil.WmiQuery<CommandLineProperty> commandLineQuery = new WbemcliUtil.WmiQuery<CommandLineProperty>((String)sb, CommandLineProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(commandLineQuery);
    }

    public static WbemcliUtil.WmiResult<ProcessXPProperty> queryProcesses(Collection<Integer> pids) {
        Object sb = WIN32_PROCESS;
        if (pids != null) {
            sb = (String)sb + " WHERE ProcessID=" + pids.stream().map(String::valueOf).collect(Collectors.joining(" OR PROCESSID="));
        }
        WbemcliUtil.WmiQuery<ProcessXPProperty> processQueryXP = new WbemcliUtil.WmiQuery<ProcessXPProperty>((String)sb, ProcessXPProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(processQueryXP);
    }

    public static enum CommandLineProperty {
        PROCESSID,
        COMMANDLINE;

    }

    public static enum ProcessXPProperty {
        PROCESSID,
        NAME,
        KERNELMODETIME,
        USERMODETIME,
        THREADCOUNT,
        PAGEFILEUSAGE,
        HANDLECOUNT,
        EXECUTABLEPATH;

    }
}

