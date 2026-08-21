/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Winevt;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Wevtapi
extends StdCallLibrary {
    public static final Wevtapi INSTANCE = Native.load("wevtapi", Wevtapi.class, W32APIOptions.UNICODE_OPTIONS);

    public Winevt.EVT_HANDLE EvtOpenSession(int var1, Winevt.EVT_RPC_LOGIN var2, int var3, int var4);

    public boolean EvtClose(Winevt.EVT_HANDLE var1);

    public boolean EvtCancel(Winevt.EVT_HANDLE var1);

    public int EvtGetExtendedStatus(int var1, char[] var2, IntByReference var3);

    public Winevt.EVT_HANDLE EvtQuery(Winevt.EVT_HANDLE var1, String var2, String var3, int var4);

    public boolean EvtNext(Winevt.EVT_HANDLE var1, int var2, Winevt.EVT_HANDLE[] var3, int var4, int var5, IntByReference var6);

    public boolean EvtSeek(Winevt.EVT_HANDLE var1, long var2, Winevt.EVT_HANDLE var4, int var5, int var6);

    public Winevt.EVT_HANDLE EvtSubscribe(Winevt.EVT_HANDLE var1, Winevt.EVT_HANDLE var2, String var3, String var4, Winevt.EVT_HANDLE var5, Pointer var6, Callback var7, int var8);

    public Winevt.EVT_HANDLE EvtCreateRenderContext(int var1, String[] var2, int var3);

    public boolean EvtRender(Winevt.EVT_HANDLE var1, Winevt.EVT_HANDLE var2, int var3, int var4, Pointer var5, IntByReference var6, IntByReference var7);

    public boolean EvtFormatMessage(Winevt.EVT_HANDLE var1, Winevt.EVT_HANDLE var2, int var3, int var4, Winevt.EVT_VARIANT[] var5, int var6, int var7, char[] var8, IntByReference var9);

    public Winevt.EVT_HANDLE EvtOpenLog(Winevt.EVT_HANDLE var1, String var2, int var3);

    public boolean EvtGetLogInfo(Winevt.EVT_HANDLE var1, int var2, int var3, Pointer var4, IntByReference var5);

    public boolean EvtClearLog(Winevt.EVT_HANDLE var1, String var2, String var3, int var4);

    public boolean EvtExportLog(Winevt.EVT_HANDLE var1, String var2, String var3, String var4, int var5);

    public boolean EvtArchiveExportedLog(Winevt.EVT_HANDLE var1, String var2, int var3, int var4);

    public Winevt.EVT_HANDLE EvtOpenChannelEnum(Winevt.EVT_HANDLE var1, int var2);

    public boolean EvtNextChannelPath(Winevt.EVT_HANDLE var1, int var2, char[] var3, IntByReference var4);

    public Winevt.EVT_HANDLE EvtOpenChannelConfig(Winevt.EVT_HANDLE var1, String var2, int var3);

    public boolean EvtSaveChannelConfig(Winevt.EVT_HANDLE var1, int var2);

    public boolean EvtSetChannelConfigProperty(Winevt.EVT_HANDLE var1, int var2, int var3, Winevt.EVT_VARIANT var4);

    public boolean EvtGetChannelConfigProperty(Winevt.EVT_HANDLE var1, int var2, int var3, int var4, Pointer var5, IntByReference var6);

    public Winevt.EVT_HANDLE EvtOpenPublisherEnum(Winevt.EVT_HANDLE var1, int var2);

    public boolean EvtNextPublisherId(Winevt.EVT_HANDLE var1, int var2, char[] var3, IntByReference var4);

    public Winevt.EVT_HANDLE EvtOpenPublisherMetadata(Winevt.EVT_HANDLE var1, String var2, String var3, int var4, int var5);

    public boolean EvtGetPublisherMetadataProperty(Winevt.EVT_HANDLE var1, int var2, int var3, int var4, Pointer var5, IntByReference var6);

    public Winevt.EVT_HANDLE EvtOpenEventMetadataEnum(Winevt.EVT_HANDLE var1, int var2);

    public Winevt.EVT_HANDLE EvtNextEventMetadata(Winevt.EVT_HANDLE var1, int var2);

    public boolean EvtGetEventMetadataProperty(Winevt.EVT_HANDLE var1, int var2, int var3, int var4, Pointer var5, IntByReference var6);

    public boolean EvtGetObjectArraySize(Pointer var1, IntByReference var2);

    public boolean EvtGetObjectArrayProperty(Pointer var1, int var2, int var3, int var4, int var5, Pointer var6, IntByReference var7);

    public boolean EvtGetQueryInfo(Winevt.EVT_HANDLE var1, int var2, int var3, Pointer var4, IntByReference var5);

    public Winevt.EVT_HANDLE EvtCreateBookmark(String var1);

    public boolean EvtUpdateBookmark(Winevt.EVT_HANDLE var1, Winevt.EVT_HANDLE var2);

    public boolean EvtGetEventInfo(Winevt.EVT_HANDLE var1, int var2, int var3, Pointer var4, IntByReference var5);
}

