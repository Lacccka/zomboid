/*
 * Decompiled with CFR 0.152.
 */
package oshi.util;

import com.sun.jna.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class ExecutingCommand {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutingCommand.class);
    private static final String[] DEFAULT_ENV = ExecutingCommand.getDefaultEnv();

    private ExecutingCommand() {
    }

    private static String[] getDefaultEnv() {
        if (Platform.isWindows()) {
            return new String[]{"LANGUAGE=C"};
        }
        return new String[]{"LC_ALL=C"};
    }

    public static List<String> runNative(String cmdToRun) {
        String[] cmd = cmdToRun.split(" ");
        return ExecutingCommand.runNative(cmd);
    }

    public static List<String> runNative(String[] cmdToRunWithArgs) {
        return ExecutingCommand.runNative(cmdToRunWithArgs, DEFAULT_ENV);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<String> runNative(String[] cmdToRunWithArgs, String[] envp) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs, envp);
            List<String> list = ExecutingCommand.getProcessOutput(p, cmdToRunWithArgs);
            return list;
        }
        catch (IOException | SecurityException e) {
            LOG.trace("Couldn't run command {}: {}", (Object)Arrays.toString(cmdToRunWithArgs), (Object)e.getMessage());
        }
        finally {
            if (p != null) {
                if (Platform.isWindows() || Platform.isSolaris()) {
                    try {
                        p.getOutputStream().close();
                    }
                    catch (IOException iOException) {}
                    try {
                        p.getInputStream().close();
                    }
                    catch (IOException iOException) {}
                    try {
                        p.getErrorStream().close();
                    }
                    catch (IOException iOException) {}
                }
                p.destroy();
            }
        }
        return Collections.emptyList();
    }

    private static List<String> getProcessOutput(Process p, String[] cmd) {
        ArrayList<String> sa = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));){
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        }
        catch (IOException e) {
            LOG.trace("Problem reading output from {}: {}", (Object)Arrays.toString(cmd), (Object)e.getMessage());
        }
        catch (InterruptedException ie) {
            LOG.trace("Interrupted while reading output from {}: {}", (Object)Arrays.toString(cmd), (Object)ie.getMessage());
            Thread.currentThread().interrupt();
        }
        return sa;
    }

    public static String getFirstAnswer(String cmd2launch) {
        return ExecutingCommand.getAnswerAt(cmd2launch, 0);
    }

    public static String getAnswerAt(String cmd2launch, int answerIdx) {
        List<String> sa = ExecutingCommand.runNative(cmd2launch);
        if (answerIdx >= 0 && answerIdx < sa.size()) {
            return sa.get(answerIdx);
        }
        return "";
    }
}

