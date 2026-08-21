/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.win32.W32APITypeMapper;

public interface Ddeml
extends StdCallLibrary {
    public static final Ddeml INSTANCE = Native.load("user32", Ddeml.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int XST_NULL = 0;
    public static final int XST_INCOMPLETE = 1;
    public static final int XST_CONNECTED = 2;
    public static final int XST_INIT1 = 3;
    public static final int XST_INIT2 = 4;
    public static final int XST_REQSENT = 5;
    public static final int XST_DATARCVD = 6;
    public static final int XST_POKESENT = 7;
    public static final int XST_POKEACKRCVD = 8;
    public static final int XST_EXECSENT = 9;
    public static final int XST_EXECACKRCVD = 10;
    public static final int XST_ADVSENT = 11;
    public static final int XST_UNADVSENT = 12;
    public static final int XST_ADVACKRCVD = 13;
    public static final int XST_UNADVACKRCVD = 14;
    public static final int XST_ADVDATASENT = 15;
    public static final int XST_ADVDATAACKRCVD = 16;
    public static final int CADV_LATEACK = 65535;
    public static final int ST_CONNECTED = 1;
    public static final int ST_ADVISE = 2;
    public static final int ST_ISLOCAL = 4;
    public static final int ST_BLOCKED = 8;
    public static final int ST_CLIENT = 16;
    public static final int ST_TERMINATED = 32;
    public static final int ST_INLIST = 64;
    public static final int ST_BLOCKNEXT = 128;
    public static final int ST_ISSELF = 256;
    public static final int DDE_FACK = 32768;
    public static final int DDE_FBUSY = 16384;
    public static final int DDE_FDEFERUPD = 16384;
    public static final int DDE_FACKREQ = 32768;
    public static final int DDE_FRELEASE = 8192;
    public static final int DDE_FREQUESTED = 4096;
    public static final int DDE_FAPPSTATUS = 255;
    public static final int DDE_FNOTPROCESSED = 0;
    public static final int DDE_FACKRESERVED = -49408;
    public static final int DDE_FADVRESERVED = -49153;
    public static final int DDE_FDATRESERVED = -45057;
    public static final int DDE_FPOKRESERVED = -8193;
    public static final int MSGF_DDEMGR = 32769;
    public static final int CP_WINANSI = 1004;
    public static final int CP_WINUNICODE = 1200;
    public static final int CP_WINNEUTRAL = 1200;
    public static final int XTYPF_NOBLOCK = 2;
    public static final int XTYPF_NODATA = 4;
    public static final int XTYPF_ACKREQ = 8;
    public static final int XCLASS_MASK = 64512;
    public static final int XCLASS_BOOL = 4096;
    public static final int XCLASS_DATA = 8192;
    public static final int XCLASS_FLAGS = 16384;
    public static final int XCLASS_NOTIFICATION = 32768;
    public static final int XTYP_ERROR = 32770;
    public static final int XTYP_ADVDATA = 16400;
    public static final int XTYP_ADVREQ = 8226;
    public static final int XTYP_ADVSTART = 4144;
    public static final int XTYP_ADVSTOP = 32832;
    public static final int XTYP_EXECUTE = 16464;
    public static final int XTYP_CONNECT = 4194;
    public static final int XTYP_CONNECT_CONFIRM = 32882;
    public static final int XTYP_XACT_COMPLETE = 32896;
    public static final int XTYP_POKE = 16528;
    public static final int XTYP_REGISTER = 32930;
    public static final int XTYP_REQUEST = 8368;
    public static final int XTYP_DISCONNECT = 32962;
    public static final int XTYP_UNREGISTER = 32978;
    public static final int XTYP_WILDCONNECT = 8418;
    public static final int XTYP_MONITOR = 33010;
    public static final int XTYP_MASK = 240;
    public static final int XTYP_SHIFT = 4;
    public static final int TIMEOUT_ASYNC = -1;
    public static final int QID_SYNC = -1;
    public static final String SZDDESYS_TOPIC = "System";
    public static final String SZDDESYS_ITEM_TOPICS = "Topics";
    public static final String SZDDESYS_ITEM_SYSITEMS = "SysItems";
    public static final String SZDDESYS_ITEM_RTNMSG = "ReturnMessage";
    public static final String SZDDESYS_ITEM_STATUS = "Status";
    public static final String SZDDESYS_ITEM_FORMATS = "Formats";
    public static final String SZDDESYS_ITEM_HELP = "Help";
    public static final String SZDDE_ITEM_ITEMLIST = "TopicItemList";
    public static final int DMLERR_NO_ERROR = 0;
    public static final int DMLERR_FIRST = 16384;
    public static final int DMLERR_ADVACKTIMEOUT = 16384;
    public static final int DMLERR_BUSY = 16385;
    public static final int DMLERR_DATAACKTIMEOUT = 16386;
    public static final int DMLERR_DLL_NOT_INITIALIZED = 16387;
    public static final int DMLERR_DLL_USAGE = 16388;
    public static final int DMLERR_EXECACKTIMEOUT = 16389;
    public static final int DMLERR_INVALIDPARAMETER = 16390;
    public static final int DMLERR_LOW_MEMORY = 16391;
    public static final int DMLERR_MEMORY_ERROR = 16392;
    public static final int DMLERR_NOTPROCESSED = 16393;
    public static final int DMLERR_NO_CONV_ESTABLISHED = 16394;
    public static final int DMLERR_POKEACKTIMEOUT = 16395;
    public static final int DMLERR_POSTMSG_FAILED = 16396;
    public static final int DMLERR_REENTRANCY = 16397;
    public static final int DMLERR_SERVER_DIED = 16398;
    public static final int DMLERR_SYS_ERROR = 16399;
    public static final int DMLERR_UNADVACKTIMEOUT = 16400;
    public static final int DMLERR_UNFOUND_QUEUE_ID = 16401;
    public static final int DMLERR_LAST = 16401;
    public static final int HDATA_APPOWNED = 1;
    public static final int CBF_FAIL_SELFCONNECTIONS = 4096;
    public static final int CBF_FAIL_CONNECTIONS = 8192;
    public static final int CBF_FAIL_ADVISES = 16384;
    public static final int CBF_FAIL_EXECUTES = 32768;
    public static final int CBF_FAIL_POKES = 65536;
    public static final int CBF_FAIL_REQUESTS = 131072;
    public static final int CBF_FAIL_ALLSVRXACTIONS = 258048;
    public static final int CBF_SKIP_CONNECT_CONFIRMS = 262144;
    public static final int CBF_SKIP_REGISTRATIONS = 524288;
    public static final int CBF_SKIP_UNREGISTRATIONS = 0x100000;
    public static final int CBF_SKIP_DISCONNECTS = 0x200000;
    public static final int CBF_SKIP_ALLNOTIFICATIONS = 0x3C0000;
    public static final int APPCMD_CLIENTONLY = 16;
    public static final int APPCMD_FILTERINITS = 32;
    public static final int APPCMD_MASK = 4080;
    public static final int APPCLASS_STANDARD = 0;
    public static final int APPCLASS_MONITOR = 1;
    public static final int APPCLASS_MASK = 15;
    public static final int MF_HSZ_INFO = 0x1000000;
    public static final int MF_SENDMSGS = 0x2000000;
    public static final int MF_POSTMSGS = 0x4000000;
    public static final int MF_CALLBACKS = 0x8000000;
    public static final int MF_ERRORS = 0x10000000;
    public static final int MF_LINKS = 0x20000000;
    public static final int MF_CONV = 0x40000000;
    public static final int MF_MASK = -16777216;
    public static final int EC_ENABLEALL = 0;
    public static final int EC_ENABLEONE = 128;
    public static final int EC_DISABLE = 8;
    public static final int EC_QUERYWAITING = 2;
    public static final int DNS_REGISTER = 1;
    public static final int DNS_UNREGISTER = 2;
    public static final int DNS_FILTERON = 4;
    public static final int DNS_FILTEROFF = 8;

    public int DdeInitialize(WinDef.DWORDByReference var1, DdeCallback var2, int var3, int var4);

    public boolean DdeUninitialize(int var1);

    public HCONVLIST DdeConnectList(int var1, HSZ var2, HSZ var3, HCONVLIST var4, CONVCONTEXT var5);

    public HCONV DdeQueryNextServer(HCONVLIST var1, HCONV var2);

    public boolean DdeDisconnectList(HCONVLIST var1);

    public HCONV DdeConnect(int var1, HSZ var2, HSZ var3, CONVCONTEXT var4);

    public boolean DdeDisconnect(HCONV var1);

    public HCONV DdeReconnect(HCONV var1);

    public int DdeQueryConvInfo(HCONV var1, int var2, CONVINFO var3);

    public boolean DdeSetUserHandle(HCONV var1, int var2, BaseTSD.DWORD_PTR var3);

    public boolean DdeAbandonTransaction(int var1, HCONV var2, int var3);

    public boolean DdePostAdvise(int var1, HSZ var2, HSZ var3);

    public boolean DdeEnableCallback(int var1, HCONV var2, int var3);

    public boolean DdeImpersonateClient(HCONV var1);

    public HDDEDATA DdeNameService(int var1, HSZ var2, HSZ var3, int var4);

    public HDDEDATA DdeClientTransaction(Pointer var1, int var2, HCONV var3, HSZ var4, int var5, int var6, int var7, WinDef.DWORDByReference var8);

    public HDDEDATA DdeCreateDataHandle(int var1, Pointer var2, int var3, int var4, HSZ var5, int var6, int var7);

    public HDDEDATA DdeAddData(HDDEDATA var1, Pointer var2, int var3, int var4);

    public int DdeGetData(HDDEDATA var1, Pointer var2, int var3, int var4);

    public Pointer DdeAccessData(HDDEDATA var1, WinDef.DWORDByReference var2);

    public boolean DdeUnaccessData(HDDEDATA var1);

    public boolean DdeFreeDataHandle(HDDEDATA var1);

    public int DdeGetLastError(int var1);

    public HSZ DdeCreateStringHandle(int var1, String var2, int var3);

    public int DdeQueryString(int var1, HSZ var2, Pointer var3, int var4, int var5);

    public boolean DdeFreeStringHandle(int var1, HSZ var2);

    public boolean DdeKeepStringHandle(int var1, HSZ var2);

    public static interface DdeCallback
    extends StdCallLibrary.StdCallCallback {
        public WinDef.PVOID ddeCallback(int var1, int var2, HCONV var3, HSZ var4, HSZ var5, HDDEDATA var6, BaseTSD.ULONG_PTR var7, BaseTSD.ULONG_PTR var8);
    }

    @Structure.FieldOrder(value={"uiLo", "uiHi", "cbData", "Data"})
    public static class DDEML_MSG_HOOK_DATA
    extends Structure {
        public WinDef.UINT_PTR uiLo;
        public WinDef.UINT_PTR uiHi;
        public int cbData;
        public byte[] Data = new byte[32];
    }

    @Structure.FieldOrder(value={"cb", "hwndTo", "dwTime", "hTask", "wMsg", "wParam", "lParam", "dmhd"})
    public static class MONMSGSTRUCT
    extends Structure {
        public int cb;
        public WinDef.HWND hwndTo;
        public int dwTime;
        public WinNT.HANDLE hTask;
        public int wMsg;
        public WinDef.WPARAM wParam;
        public WinDef.LPARAM lParam;
        public DDEML_MSG_HOOK_DATA dmhd;
    }

    @Structure.FieldOrder(value={"cb", "dwTime", "hTask", "fEstablished", "fNoData", "hszSvc", "hszTopic", "hszItem", "wFmt", "fServer", "hConvServer", "hConvClient"})
    public static class MONLINKSTRUCT
    extends Structure {
        public int cb;
        public int dwTime;
        public WinNT.HANDLE hTask;
        public WinDef.BOOL fEstablished;
        public WinDef.BOOL fNoData;
        public HSZ hszSvc;
        public HSZ hszTopic;
        public HSZ hszItem;
        public int wFmt;
        public WinDef.BOOL fServer;
        public HCONV hConvServer;
        public HCONV hConvClient;
    }

    @Structure.FieldOrder(value={"cb", "fsAction", "dwTime", "hsz", "hTask", "str"})
    public static class MONHSZSTRUCT
    extends Structure {
        public int cb;
        public int fsAction;
        public int dwTime;
        public HSZ hsz;
        public WinNT.HANDLE hTask;
        public byte[] str = new byte[1];

        @Override
        public void write() {
            this.cb = this.calculateSize(true);
            super.write();
        }

        @Override
        public void read() {
            this.readField("cb");
            this.allocateMemory(this.cb);
            super.read();
        }

        public String getStr() {
            int offset = this.fieldOffset("str");
            if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
                return this.getPointer().getWideString(offset);
            }
            return this.getPointer().getString(offset);
        }
    }

    @Structure.FieldOrder(value={"cb", "wLastError", "dwTime", "hTask"})
    public static class MONERRSTRUCT
    extends Structure {
        public int cb;
        public int wLastError;
        public int dwTime;
        public WinNT.HANDLE hTask;
    }

    @Structure.FieldOrder(value={"cb", "fConnect", "dwTime", "hTask", "hszSvc", "hszTopic", "hConvClient", "hConvServer"})
    public static class MONCONVSTRUCT
    extends Structure {
        public WinDef.UINT cb;
        public WinDef.BOOL fConnect;
        public WinDef.DWORD dwTime;
        public WinNT.HANDLE hTask;
        public HSZ hszSvc;
        public HSZ hszTopic;
        public HCONV hConvClient;
        public HCONV hConvServer;
    }

    @Structure.FieldOrder(value={"cb", "dwTime", "hTask", "dwRet", "wType", "wFmt", "hConv", "hsz1", "hsz2", "hData", "dwData1", "dwData2", "cc", "cbData", "Data"})
    public static class MONCBSTRUCT
    extends Structure {
        public int cb;
        public int dwTime;
        public WinNT.HANDLE hTask;
        public WinDef.DWORD dwRet;
        public int wType;
        public int wFmt;
        public HCONV hConv;
        public HSZ hsz1;
        public HSZ hsz2;
        public HDDEDATA hData;
        public BaseTSD.ULONG_PTR dwData1;
        public BaseTSD.ULONG_PTR dwData2;
        public CONVCONTEXT cc;
        public int cbData;
        public byte[] Data = new byte[32];
    }

    @Structure.FieldOrder(value={"cb", "hUser", "hConvPartner", "hszSvcPartner", "hszServiceReq", "hszTopic", "hszItem", "wFmt", "wType", "wStatus", "wConvst", "wLastError", "hConvList", "ConvCtxt", "hwnd", "hwndPartner"})
    public static class CONVINFO
    extends Structure {
        public int cb;
        public BaseTSD.DWORD_PTR hUser;
        public HCONV hConvPartner;
        public HSZ hszSvcPartner;
        public HSZ hszServiceReq;
        public HSZ hszTopic;
        public HSZ hszItem;
        public int wFmt;
        public int wType;
        public int wStatus;
        public int wConvst;
        public int wLastError;
        public HCONVLIST hConvList;
        public CONVCONTEXT ConvCtxt;
        public WinDef.HWND hwnd;
        public WinDef.HWND hwndPartner;

        @Override
        public void write() {
            this.cb = this.size();
            super.write();
        }
    }

    @Structure.FieldOrder(value={"cb", "wFlags", "wCountryID", "iCodePage", "dwLangID", "dwSecurity", "qos"})
    public static class CONVCONTEXT
    extends Structure {
        public int cb;
        public int wFlags;
        public int wCountryID;
        public int iCodePage;
        public int dwLangID;
        public int dwSecurity;
        public WinNT.SECURITY_QUALITY_OF_SERVICE qos;

        public CONVCONTEXT() {
        }

        public CONVCONTEXT(Pointer p) {
            super(p);
        }

        @Override
        public void write() {
            this.cb = this.size();
            super.write();
        }
    }

    @Structure.FieldOrder(value={"service", "topic"})
    public static class HSZPAIR
    extends Structure {
        public HSZ service;
        public HSZ topic;

        public HSZPAIR() {
        }

        public HSZPAIR(HSZ service, HSZ topic) {
            this.service = service;
            this.topic = topic;
        }
    }

    public static class HDDEDATA
    extends WinDef.PVOID {
    }

    public static class HSZ
    extends PointerType {
    }

    public static class HCONV
    extends PointerType {
    }

    public static class HCONVLIST
    extends PointerType {
    }
}

