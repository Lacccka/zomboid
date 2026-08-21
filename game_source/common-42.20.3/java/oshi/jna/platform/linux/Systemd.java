/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface Systemd
extends Library {
    public static final Systemd INSTANCE = Native.load("systemd", Systemd.class);

    public int sd_session_get_start_time(String var1, LongByReference var2);

    public int sd_session_get_username(String var1, PointerByReference var2);

    public int sd_session_get_tty(String var1, PointerByReference var2);

    public int sd_session_get_remote_host(String var1, PointerByReference var2);

    public int sd_get_sessions(PointerByReference var1);
}

