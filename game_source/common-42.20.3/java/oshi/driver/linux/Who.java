/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.linux.LinuxLibc;
import oshi.jna.platform.linux.Systemd;
import oshi.software.os.OSSession;
import oshi.util.Constants;
import oshi.util.FileUtil;
import oshi.util.GlobalConfig;
import oshi.util.ParseUtil;
import oshi.util.Util;

@ThreadSafe
public final class Who {
    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;
    private static boolean useSystemd = GlobalConfig.get("oshi.os.linux.allowsystemd", true);

    private Who() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static synchronized List<OSSession> queryUtxent() {
        if (useSystemd) {
            try {
                List<OSSession> systemdSessions = Who.querySystemdNative();
                if (!systemdSessions.isEmpty()) {
                    return systemdSessions;
                }
            }
            catch (Throwable t) {
                useSystemd = false;
            }
        }
        List<OSSession> whoList = new ArrayList<OSSession>();
        LIBC.setutxent();
        try {
            LinuxLibc.LinuxUtmpx ut;
            while (Objects.nonNull(ut = LIBC.getutxent())) {
                if (ut.ut_type != 7 && ut.ut_type != 6) continue;
                String user = Native.toString(ut.ut_user, Charset.defaultCharset());
                String device = Native.toString(ut.ut_line, Charset.defaultCharset());
                String host = ParseUtil.parseUtAddrV6toIP(ut.ut_addr_v6);
                long loginTime = (long)ut.ut_tv.tv_sec * 1000L + (long)ut.ut_tv.tv_usec / 1000L;
                if (!Util.isSessionValid(user, device, loginTime)) {
                    List<OSSession> list = oshi.driver.unix.Who.queryWho();
                    return list;
                }
                whoList.add(new OSSession(user, device, loginTime, host));
            }
        }
        finally {
            LIBC.endutxent();
        }
        if (whoList.isEmpty() && (whoList = Who.querySystemdFiles()).isEmpty()) {
            return oshi.driver.unix.Who.queryWho();
        }
        return whoList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static List<OSSession> querySystemdNative() {
        ArrayList<OSSession> sessionList;
        block49: {
            sessionList = new ArrayList<OSSession>();
            try (ByRef.CloseablePointerByReference sessionsPtr = new ByRef.CloseablePointerByReference();){
                Pointer sessions;
                int count = Systemd.INSTANCE.sd_get_sessions(sessionsPtr);
                if (count <= 0 || Pointer.nativeValue(sessions = sessionsPtr.getValue()) == 0L) break block49;
                try {
                    String[] sessionIds = sessions.getStringArray(0L, count);
                    for (String sessionId : sessionIds) {
                        if (Objects.isNull(sessionId)) continue;
                        try {
                            long loginTime;
                            String user;
                            Pointer usernamePointer = null;
                            try (ByRef.CloseablePointerByReference usernamePtr = new ByRef.CloseablePointerByReference();){
                                if (Systemd.INSTANCE.sd_session_get_username(sessionId, usernamePtr) != 0 || Pointer.nativeValue(usernamePtr.getValue()) == 0L) continue;
                                usernamePointer = usernamePtr.getValue();
                            }
                            try {
                                user = usernamePointer.getString(0L);
                            }
                            finally {
                                Native.free(Pointer.nativeValue(usernamePointer));
                            }
                            try (ByRef.CloseableLongByReference startTimePtr = new ByRef.CloseableLongByReference();){
                                if (Systemd.INSTANCE.sd_session_get_start_time(sessionId, startTimePtr) != 0) continue;
                                loginTime = startTimePtr.getValue() / 1000L;
                            }
                            String tty = sessionId;
                            Pointer ttyPointer = null;
                            try (ByRef.CloseablePointerByReference ttyPtr = new ByRef.CloseablePointerByReference();){
                                if (Systemd.INSTANCE.sd_session_get_tty(sessionId, ttyPtr) == 0 && Pointer.nativeValue(ttyPtr.getValue()) != 0L) {
                                    ttyPointer = ttyPtr.getValue();
                                }
                            }
                            if (Pointer.nativeValue(ttyPointer) != 0L) {
                                try {
                                    tty = ttyPointer.getString(0L);
                                }
                                finally {
                                    Native.free(Pointer.nativeValue(ttyPointer));
                                }
                            }
                            String remoteHost = "";
                            Pointer remoteHostPointer = null;
                            try (ByRef.CloseablePointerByReference remoteHostPtr = new ByRef.CloseablePointerByReference();){
                                if (Systemd.INSTANCE.sd_session_get_remote_host(sessionId, remoteHostPtr) == 0 && Pointer.nativeValue(remoteHostPtr.getValue()) != 0L) {
                                    remoteHostPointer = remoteHostPtr.getValue();
                                }
                            }
                            if (Pointer.nativeValue(remoteHostPointer) != 0L) {
                                try {
                                    remoteHost = remoteHostPointer.getString(0L);
                                }
                                finally {
                                    Native.free(Pointer.nativeValue(remoteHostPointer));
                                }
                            }
                            if (!Util.isSessionValid(user, tty, loginTime)) continue;
                            sessionList.add(new OSSession(user, tty, loginTime, remoteHost));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                finally {
                    Pointer[] ptrs = sessions.getPointerArray(0L, count);
                    for (Pointer stringPtr : ptrs) {
                        if (Pointer.nativeValue(stringPtr) == 0L) continue;
                        Native.free(Pointer.nativeValue(stringPtr));
                    }
                    Native.free(Pointer.nativeValue(sessions));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return sessionList;
    }

    private static List<OSSession> querySystemdFiles() {
        File[] sessionFiles;
        ArrayList<OSSession> sessionList = new ArrayList<OSSession>();
        File sessionsDir = new File("/run/systemd/sessions");
        if (sessionsDir.exists() && sessionsDir.isDirectory() && Objects.nonNull(sessionFiles = sessionsDir.listFiles(file -> Constants.DIGITS.matcher(file.getName()).matches()))) {
            for (File sessionFile : sessionFiles) {
                try {
                    Map<String, String> sessionMap = FileUtil.getKeyValueMapFromFile(sessionFile.getPath(), "=");
                    String user = sessionMap.get("USER");
                    if (!Objects.nonNull(user) || user.isEmpty()) continue;
                    String tty = sessionMap.getOrDefault("TTY", sessionFile.getName());
                    String remoteHost = sessionMap.getOrDefault("REMOTE_HOST", "");
                    long loginTime = 0L;
                    String realtime = sessionMap.get("REALTIME");
                    if (Objects.nonNull(realtime)) {
                        loginTime = ParseUtil.parseLongOrDefault(realtime, 0L) / 1000L;
                    }
                    if (loginTime == 0L) {
                        loginTime = sessionFile.lastModified();
                    }
                    if (!Util.isSessionValid(user, tty, loginTime)) continue;
                    sessionList.add(new OSSession(user, tty, loginTime, remoteHost));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return sessionList;
    }
}

