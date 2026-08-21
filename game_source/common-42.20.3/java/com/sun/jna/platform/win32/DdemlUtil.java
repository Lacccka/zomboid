/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Ddeml;
import com.sun.jna.platform.win32.User32Util;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DdemlUtil {

    public static interface IDdeConnectionList
    extends Closeable {
        public Ddeml.HCONVLIST getHandle();

        public IDdeConnection queryNextServer(IDdeConnection var1);

        @Override
        public void close();
    }

    public static interface IDdeClient
    extends Closeable {
        public Integer getInstanceIdentitifier();

        public void initialize(int var1) throws DdemlException;

        public Ddeml.HSZ createStringHandle(String var1) throws DdemlException;

        public String queryString(Ddeml.HSZ var1) throws DdemlException;

        public boolean freeStringHandle(Ddeml.HSZ var1);

        public boolean keepStringHandle(Ddeml.HSZ var1);

        public void nameService(Ddeml.HSZ var1, int var2) throws DdemlException;

        public void nameService(String var1, int var2) throws DdemlException;

        public int getLastError();

        public IDdeConnection connect(Ddeml.HSZ var1, Ddeml.HSZ var2, Ddeml.CONVCONTEXT var3);

        public IDdeConnection connect(String var1, String var2, Ddeml.CONVCONTEXT var3);

        public Ddeml.HDDEDATA createDataHandle(Pointer var1, int var2, int var3, Ddeml.HSZ var4, int var5, int var6);

        public void freeDataHandle(Ddeml.HDDEDATA var1);

        public Ddeml.HDDEDATA addData(Ddeml.HDDEDATA var1, Pointer var2, int var3, int var4);

        public int getData(Ddeml.HDDEDATA var1, Pointer var2, int var3, int var4);

        public Pointer accessData(Ddeml.HDDEDATA var1, WinDef.DWORDByReference var2);

        public void unaccessData(Ddeml.HDDEDATA var1);

        public void postAdvise(Ddeml.HSZ var1, Ddeml.HSZ var2);

        public void postAdvise(String var1, String var2);

        public void abandonTransactions();

        public IDdeConnectionList connectList(Ddeml.HSZ var1, Ddeml.HSZ var2, IDdeConnectionList var3, Ddeml.CONVCONTEXT var4);

        public IDdeConnectionList connectList(String var1, String var2, IDdeConnectionList var3, Ddeml.CONVCONTEXT var4);

        public boolean enableCallback(int var1);

        public boolean uninitialize();

        public IDdeConnection wrap(Ddeml.HCONV var1);

        public void registerAdvstartHandler(AdvstartHandler var1);

        public void unregisterAdvstartHandler(AdvstartHandler var1);

        public void registerAdvstopHandler(AdvstopHandler var1);

        public void unregisterAdvstopHandler(AdvstopHandler var1);

        public void registerConnectHandler(ConnectHandler var1);

        public void unregisterConnectHandler(ConnectHandler var1);

        public void registerAdvReqHandler(AdvreqHandler var1);

        public void unregisterAdvReqHandler(AdvreqHandler var1);

        public void registerRequestHandler(RequestHandler var1);

        public void unregisterRequestHandler(RequestHandler var1);

        public void registerWildconnectHandler(WildconnectHandler var1);

        public void unregisterWildconnectHandler(WildconnectHandler var1);

        public void registerAdvdataHandler(AdvdataHandler var1);

        public void unregisterAdvdataHandler(AdvdataHandler var1);

        public void registerExecuteHandler(ExecuteHandler var1);

        public void unregisterExecuteHandler(ExecuteHandler var1);

        public void registerPokeHandler(PokeHandler var1);

        public void unregisterPokeHandler(PokeHandler var1);

        public void registerConnectConfirmHandler(ConnectConfirmHandler var1);

        public void unregisterConnectConfirmHandler(ConnectConfirmHandler var1);

        public void registerDisconnectHandler(DisconnectHandler var1);

        public void unregisterDisconnectHandler(DisconnectHandler var1);

        public void registerErrorHandler(ErrorHandler var1);

        public void unregisterErrorHandler(ErrorHandler var1);

        public void registerRegisterHandler(RegisterHandler var1);

        public void unregisterRegisterHandler(RegisterHandler var1);

        public void registerXactCompleteHandler(XactCompleteHandler var1);

        public void unregisterXactCompleteHandler(XactCompleteHandler var1);

        public void registerUnregisterHandler(UnregisterHandler var1);

        public void unregisterUnregisterHandler(UnregisterHandler var1);

        public void registerMonitorHandler(MonitorHandler var1);

        public void unregisterMonitorHandler(MonitorHandler var1);
    }

    public static interface IDdeConnection
    extends Closeable {
        public Ddeml.HCONV getConv();

        public void execute(String var1, int var2, WinDef.DWORDByReference var3, BaseTSD.DWORD_PTR var4);

        public void poke(Pointer var1, int var2, Ddeml.HSZ var3, int var4, int var5, WinDef.DWORDByReference var6, BaseTSD.DWORD_PTR var7);

        public void poke(Pointer var1, int var2, String var3, int var4, int var5, WinDef.DWORDByReference var6, BaseTSD.DWORD_PTR var7);

        public Ddeml.HDDEDATA request(Ddeml.HSZ var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public Ddeml.HDDEDATA request(String var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public Ddeml.HDDEDATA clientTransaction(Pointer var1, int var2, Ddeml.HSZ var3, int var4, int var5, int var6, WinDef.DWORDByReference var7, BaseTSD.DWORD_PTR var8);

        public Ddeml.HDDEDATA clientTransaction(Pointer var1, int var2, String var3, int var4, int var5, int var6, WinDef.DWORDByReference var7, BaseTSD.DWORD_PTR var8);

        public void advstart(Ddeml.HSZ var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public void advstart(String var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public void advstop(Ddeml.HSZ var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public void advstop(String var1, int var2, int var3, WinDef.DWORDByReference var4, BaseTSD.DWORD_PTR var5);

        public void abandonTransaction(int var1);

        public void abandonTransactions();

        public void impersonateClient();

        @Override
        public void close();

        public void reconnect();

        public boolean enableCallback(int var1);

        public void setUserHandle(int var1, BaseTSD.DWORD_PTR var2) throws DdemlException;

        public Ddeml.CONVINFO queryConvInfo(int var1) throws DdemlException;
    }

    public static class DdemlException
    extends RuntimeException {
        private static final Map<Integer, String> ERROR_CODE_MAP;
        private final int errorCode;

        public static DdemlException create(int errorCode) {
            String errorName = ERROR_CODE_MAP.get(errorCode);
            return new DdemlException(errorCode, String.format("%s (Code: 0x%X)", errorName != null ? errorName : "", errorCode));
        }

        public DdemlException(int errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        static {
            HashMap<Integer, String> errorCodeMapBuilder = new HashMap<Integer, String>();
            for (Field f : Ddeml.class.getFields()) {
                String name = f.getName();
                if (!name.startsWith("DMLERR_") || name.equals("DMLERR_FIRST") || name.equals("DMLERR_LAST")) continue;
                try {
                    errorCodeMapBuilder.put(f.getInt(null), name);
                }
                catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                }
            }
            ERROR_CODE_MAP = Collections.unmodifiableMap(errorCodeMapBuilder);
        }
    }

    public static class DdeAdapter
    implements Ddeml.DdeCallback {
        private static final Logger LOG = Logger.getLogger(DdeAdapter.class.getName());
        private int idInst;
        private final List<AdvstartHandler> advstartHandler = new CopyOnWriteArrayList<AdvstartHandler>();
        private final List<AdvstopHandler> advstopHandler = new CopyOnWriteArrayList<AdvstopHandler>();
        private final List<ConnectHandler> connectHandler = new CopyOnWriteArrayList<ConnectHandler>();
        private final List<AdvreqHandler> advReqHandler = new CopyOnWriteArrayList<AdvreqHandler>();
        private final List<RequestHandler> requestHandler = new CopyOnWriteArrayList<RequestHandler>();
        private final List<WildconnectHandler> wildconnectHandler = new CopyOnWriteArrayList<WildconnectHandler>();
        private final List<AdvdataHandler> advdataHandler = new CopyOnWriteArrayList<AdvdataHandler>();
        private final List<ExecuteHandler> executeHandler = new CopyOnWriteArrayList<ExecuteHandler>();
        private final List<PokeHandler> pokeHandler = new CopyOnWriteArrayList<PokeHandler>();
        private final List<ConnectConfirmHandler> connectConfirmHandler = new CopyOnWriteArrayList<ConnectConfirmHandler>();
        private final List<DisconnectHandler> disconnectHandler = new CopyOnWriteArrayList<DisconnectHandler>();
        private final List<ErrorHandler> errorHandler = new CopyOnWriteArrayList<ErrorHandler>();
        private final List<RegisterHandler> registerHandler = new CopyOnWriteArrayList<RegisterHandler>();
        private final List<XactCompleteHandler> xactCompleteHandler = new CopyOnWriteArrayList<XactCompleteHandler>();
        private final List<UnregisterHandler> unregisterHandler = new CopyOnWriteArrayList<UnregisterHandler>();
        private final List<MonitorHandler> monitorHandler = new CopyOnWriteArrayList<MonitorHandler>();

        public void setInstanceIdentifier(int idInst) {
            this.idInst = idInst;
        }

        @Override
        public WinDef.PVOID ddeCallback(int wType, int wFmt, Ddeml.HCONV hConv, Ddeml.HSZ hsz1, Ddeml.HSZ hsz2, Ddeml.HDDEDATA hData, BaseTSD.ULONG_PTR lData1, BaseTSD.ULONG_PTR lData2) {
            Object transactionTypeName = null;
            try {
                switch (wType) {
                    case 4144: {
                        boolean booleanResult = this.onAdvstart(wType, wFmt, hConv, hsz1, hsz2);
                        return new WinDef.PVOID(Pointer.createConstant(new WinDef.BOOL(booleanResult).intValue()));
                    }
                    case 4194: {
                        Ddeml.CONVCONTEXT convcontext = null;
                        if (lData1.toPointer() != null) {
                            convcontext = new Ddeml.CONVCONTEXT(new Pointer(lData1.longValue()));
                        }
                        boolean booleanResult = this.onConnect(wType, hsz1, hsz2, convcontext, lData2 != null && lData2.intValue() != 0);
                        return new WinDef.PVOID(Pointer.createConstant(new WinDef.BOOL(booleanResult).intValue()));
                    }
                    case 8226: {
                        int count = lData1.intValue() & 0xFFFF;
                        Ddeml.HDDEDATA data = this.onAdvreq(wType, wFmt, hConv, hsz1, hsz2, count);
                        if (data == null) {
                            return new WinDef.PVOID();
                        }
                        return new WinDef.PVOID(data.getPointer());
                    }
                    case 8368: {
                        Ddeml.HDDEDATA data = this.onRequest(wType, wFmt, hConv, hsz1, hsz2);
                        if (data == null) {
                            return new WinDef.PVOID();
                        }
                        return new WinDef.PVOID(data.getPointer());
                    }
                    case 8418: {
                        Ddeml.HSZPAIR[] hszPairs;
                        Ddeml.CONVCONTEXT convcontext = null;
                        if (lData1.toPointer() != null) {
                            convcontext = new Ddeml.CONVCONTEXT(new Pointer(lData1.longValue()));
                        }
                        if ((hszPairs = this.onWildconnect(wType, hsz1, hsz2, convcontext, lData2 != null && lData2.intValue() != 0)) == null || hszPairs.length == 0) {
                            return new WinDef.PVOID();
                        }
                        int size = 0;
                        for (Ddeml.HSZPAIR hp : hszPairs) {
                            hp.write();
                            size += hp.size();
                        }
                        Ddeml.HDDEDATA data = Ddeml.INSTANCE.DdeCreateDataHandle(this.idInst, hszPairs[0].getPointer(), size, 0, null, wFmt, 0);
                        return new WinDef.PVOID(data.getPointer());
                    }
                    case 16400: {
                        int intResult = this.onAdvdata(wType, wFmt, hConv, hsz1, hsz2, hData);
                        return new WinDef.PVOID(Pointer.createConstant(intResult));
                    }
                    case 16464: {
                        int intResult = this.onExecute(wType, hConv, hsz1, hData);
                        Ddeml.INSTANCE.DdeFreeDataHandle(hData);
                        return new WinDef.PVOID(Pointer.createConstant(intResult));
                    }
                    case 16528: {
                        int intResult = this.onPoke(wType, wFmt, hConv, hsz1, hsz2, hData);
                        return new WinDef.PVOID(Pointer.createConstant(intResult));
                    }
                    case 32832: {
                        this.onAdvstop(wType, wFmt, hConv, hsz1, hsz2);
                        break;
                    }
                    case 32882: {
                        this.onConnectConfirm(wType, hConv, hsz1, hsz2, lData2 != null && lData2.intValue() != 0);
                        break;
                    }
                    case 32962: {
                        this.onDisconnect(wType, hConv, lData2 != null && lData2.intValue() != 0);
                        break;
                    }
                    case 32770: {
                        this.onError(wType, hConv, (int)(lData2.longValue() & 0xFFFFL));
                        break;
                    }
                    case 32930: {
                        this.onRegister(wType, hsz1, hsz2);
                        break;
                    }
                    case 32896: {
                        this.onXactComplete(wType, wFmt, hConv, hsz1, hsz2, hData, lData1, lData2);
                        break;
                    }
                    case 32978: {
                        this.onUnregister(wType, hsz1, hsz2);
                        break;
                    }
                    case 33010: {
                        this.onMonitor(wType, hData, lData2.intValue());
                        break;
                    }
                    default: {
                        LOG.log(Level.FINE, String.format("Not implemented Operation - Transaction type: 0x%X (%s)", wType, transactionTypeName));
                        break;
                    }
                }
            }
            catch (BlockException ex) {
                return new WinDef.PVOID(Pointer.createConstant(-1));
            }
            catch (Throwable ex) {
                LOG.log(Level.WARNING, "Exception in DDECallback", ex);
            }
            return new WinDef.PVOID();
        }

        public void registerAdvstartHandler(AdvstartHandler handler) {
            this.advstartHandler.add(handler);
        }

        public void unregisterAdvstartHandler(AdvstartHandler handler) {
            this.advstartHandler.remove(handler);
        }

        private boolean onAdvstart(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item) {
            boolean oneHandlerTrue = false;
            for (AdvstartHandler handler : this.advstartHandler) {
                if (!handler.onAdvstart(transactionType, dataFormat, hconv, topic, item)) continue;
                oneHandlerTrue = true;
            }
            return oneHandlerTrue;
        }

        public void registerAdvstopHandler(AdvstopHandler handler) {
            this.advstopHandler.add(handler);
        }

        public void unregisterAdvstopHandler(AdvstopHandler handler) {
            this.advstopHandler.remove(handler);
        }

        private void onAdvstop(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item) {
            for (AdvstopHandler handler : this.advstopHandler) {
                handler.onAdvstop(transactionType, dataFormat, hconv, topic, item);
            }
        }

        public void registerConnectHandler(ConnectHandler handler) {
            this.connectHandler.add(handler);
        }

        public void unregisterConnectHandler(ConnectHandler handler) {
            this.connectHandler.remove(handler);
        }

        private boolean onConnect(int transactionType, Ddeml.HSZ topic, Ddeml.HSZ service, Ddeml.CONVCONTEXT convcontext, boolean sameInstance) {
            boolean oneHandlerTrue = false;
            for (ConnectHandler handler : this.connectHandler) {
                if (!handler.onConnect(transactionType, topic, service, convcontext, sameInstance)) continue;
                oneHandlerTrue = true;
            }
            return oneHandlerTrue;
        }

        public void registerAdvReqHandler(AdvreqHandler handler) {
            this.advReqHandler.add(handler);
        }

        public void unregisterAdvReqHandler(AdvreqHandler handler) {
            this.advReqHandler.remove(handler);
        }

        private Ddeml.HDDEDATA onAdvreq(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item, int count) {
            for (AdvreqHandler handler : this.advReqHandler) {
                Ddeml.HDDEDATA result = handler.onAdvreq(transactionType, dataFormat, hconv, topic, item, count);
                if (result == null) continue;
                return result;
            }
            return null;
        }

        public void registerRequestHandler(RequestHandler handler) {
            this.requestHandler.add(handler);
        }

        public void unregisterRequestHandler(RequestHandler handler) {
            this.requestHandler.remove(handler);
        }

        private Ddeml.HDDEDATA onRequest(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item) {
            for (RequestHandler handler : this.requestHandler) {
                Ddeml.HDDEDATA result = handler.onRequest(transactionType, dataFormat, hconv, topic, item);
                if (result == null) continue;
                return result;
            }
            return null;
        }

        public void registerWildconnectHandler(WildconnectHandler handler) {
            this.wildconnectHandler.add(handler);
        }

        public void unregisterWildconnectHandler(WildconnectHandler handler) {
            this.wildconnectHandler.remove(handler);
        }

        private Ddeml.HSZPAIR[] onWildconnect(int transactionType, Ddeml.HSZ topic, Ddeml.HSZ service, Ddeml.CONVCONTEXT convcontext, boolean sameInstance) {
            ArrayList<Ddeml.HSZPAIR> hszpairs = new ArrayList<Ddeml.HSZPAIR>(1);
            for (WildconnectHandler handler : this.wildconnectHandler) {
                hszpairs.addAll(handler.onWildconnect(transactionType, topic, service, convcontext, sameInstance));
            }
            return hszpairs.toArray(new Ddeml.HSZPAIR[0]);
        }

        public void registerAdvdataHandler(AdvdataHandler handler) {
            this.advdataHandler.add(handler);
        }

        public void unregisterAdvdataHandler(AdvdataHandler handler) {
            this.advdataHandler.remove(handler);
        }

        private int onAdvdata(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item, Ddeml.HDDEDATA hdata) {
            for (AdvdataHandler handler : this.advdataHandler) {
                int result = handler.onAdvdata(transactionType, dataFormat, hconv, topic, item, hdata);
                if (result == 0) continue;
                return result;
            }
            return 0;
        }

        public void registerExecuteHandler(ExecuteHandler handler) {
            this.executeHandler.add(handler);
        }

        public void unregisterExecuteHandler(ExecuteHandler handler) {
            this.executeHandler.remove(handler);
        }

        private int onExecute(int transactionType, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HDDEDATA commandString) {
            for (ExecuteHandler handler : this.executeHandler) {
                int result = handler.onExecute(transactionType, hconv, topic, commandString);
                if (result == 0) continue;
                return result;
            }
            return 0;
        }

        public void registerPokeHandler(PokeHandler handler) {
            this.pokeHandler.add(handler);
        }

        public void unregisterPokeHandler(PokeHandler handler) {
            this.pokeHandler.remove(handler);
        }

        private int onPoke(int transactionType, int dataFormat, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ item, Ddeml.HDDEDATA hdata) {
            for (PokeHandler handler : this.pokeHandler) {
                int result = handler.onPoke(transactionType, dataFormat, hconv, topic, item, hdata);
                if (result == 0) continue;
                return result;
            }
            return 0;
        }

        public void registerConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.connectConfirmHandler.add(handler);
        }

        public void unregisterConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.connectConfirmHandler.remove(handler);
        }

        private void onConnectConfirm(int transactionType, Ddeml.HCONV hconv, Ddeml.HSZ topic, Ddeml.HSZ service, boolean sameInstance) {
            for (ConnectConfirmHandler handler : this.connectConfirmHandler) {
                handler.onConnectConfirm(transactionType, hconv, topic, service, sameInstance);
            }
        }

        public void registerDisconnectHandler(DisconnectHandler handler) {
            this.disconnectHandler.add(handler);
        }

        public void unregisterDisconnectHandler(DisconnectHandler handler) {
            this.disconnectHandler.remove(handler);
        }

        private void onDisconnect(int transactionType, Ddeml.HCONV hconv, boolean sameInstance) {
            for (DisconnectHandler handler : this.disconnectHandler) {
                handler.onDisconnect(transactionType, hconv, sameInstance);
            }
        }

        public void registerErrorHandler(ErrorHandler handler) {
            this.errorHandler.add(handler);
        }

        public void unregisterErrorHandler(ErrorHandler handler) {
            this.errorHandler.remove(handler);
        }

        private void onError(int transactionType, Ddeml.HCONV hconv, int errorCode) {
            for (ErrorHandler handler : this.errorHandler) {
                handler.onError(transactionType, hconv, errorCode);
            }
        }

        public void registerRegisterHandler(RegisterHandler handler) {
            this.registerHandler.add(handler);
        }

        public void unregisterRegisterHandler(RegisterHandler handler) {
            this.registerHandler.remove(handler);
        }

        private void onRegister(int transactionType, Ddeml.HSZ baseServiceName, Ddeml.HSZ instanceSpecificServiceName) {
            for (RegisterHandler handler : this.registerHandler) {
                handler.onRegister(transactionType, baseServiceName, instanceSpecificServiceName);
            }
        }

        public void registerXactCompleteHandler(XactCompleteHandler handler) {
            this.xactCompleteHandler.add(handler);
        }

        public void xactCompleteXactCompleteHandler(XactCompleteHandler handler) {
            this.xactCompleteHandler.remove(handler);
        }

        private void onXactComplete(int transactionType, int dataFormat, Ddeml.HCONV hConv, Ddeml.HSZ topic, Ddeml.HSZ item, Ddeml.HDDEDATA hdata, BaseTSD.ULONG_PTR transactionIdentifier, BaseTSD.ULONG_PTR statusFlag) {
            for (XactCompleteHandler handler : this.xactCompleteHandler) {
                handler.onXactComplete(transactionType, dataFormat, hConv, topic, item, hdata, transactionIdentifier, statusFlag);
            }
        }

        public void registerUnregisterHandler(UnregisterHandler handler) {
            this.unregisterHandler.add(handler);
        }

        public void unregisterUnregisterHandler(UnregisterHandler handler) {
            this.unregisterHandler.remove(handler);
        }

        private void onUnregister(int transactionType, Ddeml.HSZ baseServiceName, Ddeml.HSZ instanceSpecificServiceName) {
            for (UnregisterHandler handler : this.unregisterHandler) {
                handler.onUnregister(transactionType, baseServiceName, instanceSpecificServiceName);
            }
        }

        public void registerMonitorHandler(MonitorHandler handler) {
            this.monitorHandler.add(handler);
        }

        public void unregisterMonitorHandler(MonitorHandler handler) {
            this.monitorHandler.remove(handler);
        }

        private void onMonitor(int transactionType, Ddeml.HDDEDATA hdata, int dwData2) {
            for (MonitorHandler handler : this.monitorHandler) {
                handler.onMonitor(transactionType, hdata, dwData2);
            }
        }

        public static class BlockException
        extends RuntimeException {
        }
    }

    public static interface MonitorHandler {
        public void onMonitor(int var1, Ddeml.HDDEDATA var2, int var3);
    }

    public static interface PokeHandler {
        public int onPoke(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5, Ddeml.HDDEDATA var6);
    }

    public static interface ExecuteHandler {
        public int onExecute(int var1, Ddeml.HCONV var2, Ddeml.HSZ var3, Ddeml.HDDEDATA var4);
    }

    public static interface UnregisterHandler {
        public void onUnregister(int var1, Ddeml.HSZ var2, Ddeml.HSZ var3);
    }

    public static interface XactCompleteHandler {
        public void onXactComplete(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5, Ddeml.HDDEDATA var6, BaseTSD.ULONG_PTR var7, BaseTSD.ULONG_PTR var8);
    }

    public static interface RegisterHandler {
        public void onRegister(int var1, Ddeml.HSZ var2, Ddeml.HSZ var3);
    }

    public static interface ErrorHandler {
        public void onError(int var1, Ddeml.HCONV var2, int var3);
    }

    public static interface DisconnectHandler {
        public void onDisconnect(int var1, Ddeml.HCONV var2, boolean var3);
    }

    public static interface ConnectConfirmHandler {
        public void onConnectConfirm(int var1, Ddeml.HCONV var2, Ddeml.HSZ var3, Ddeml.HSZ var4, boolean var5);
    }

    public static interface AdvdataHandler {
        public int onAdvdata(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5, Ddeml.HDDEDATA var6);
    }

    public static interface WildconnectHandler {
        public List<Ddeml.HSZPAIR> onWildconnect(int var1, Ddeml.HSZ var2, Ddeml.HSZ var3, Ddeml.CONVCONTEXT var4, boolean var5);
    }

    public static interface RequestHandler {
        public Ddeml.HDDEDATA onRequest(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5);
    }

    public static interface AdvreqHandler {
        public Ddeml.HDDEDATA onAdvreq(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5, int var6);
    }

    public static interface ConnectHandler {
        public boolean onConnect(int var1, Ddeml.HSZ var2, Ddeml.HSZ var3, Ddeml.CONVCONTEXT var4, boolean var5);
    }

    public static interface AdvstopHandler {
        public void onAdvstop(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5);
    }

    public static interface AdvstartHandler {
        public boolean onAdvstart(int var1, int var2, Ddeml.HCONV var3, Ddeml.HSZ var4, Ddeml.HSZ var5);
    }

    public static class DdeClient
    implements IDdeClient {
        private Integer idInst;
        private final DdeAdapter ddeAdapter = new DdeAdapter();

        @Override
        public Integer getInstanceIdentitifier() {
            return this.idInst;
        }

        @Override
        public void initialize(int afCmd) throws DdemlException {
            WinDef.DWORDByReference pidInst = new WinDef.DWORDByReference();
            Integer result = Ddeml.INSTANCE.DdeInitialize(pidInst, this.ddeAdapter, afCmd, 0);
            if (result != 0) {
                throw DdemlException.create(result);
            }
            this.idInst = pidInst.getValue().intValue();
            if (this.ddeAdapter instanceof DdeAdapter) {
                this.ddeAdapter.setInstanceIdentifier(this.idInst);
            }
        }

        @Override
        public Ddeml.HSZ createStringHandle(String value) throws DdemlException {
            if (value == null) {
                return null;
            }
            int codePage = W32APIOptions.DEFAULT_OPTIONS == W32APIOptions.UNICODE_OPTIONS ? 1200 : 1004;
            Ddeml.HSZ handle = Ddeml.INSTANCE.DdeCreateStringHandle(this.idInst, value, codePage);
            if (handle == null) {
                throw DdemlException.create(this.getLastError());
            }
            return handle;
        }

        @Override
        public void nameService(Ddeml.HSZ name, int afCmd) throws DdemlException {
            Ddeml.HDDEDATA handle = Ddeml.INSTANCE.DdeNameService(this.idInst, name, new Ddeml.HSZ(), afCmd);
            if (handle == null) {
                throw DdemlException.create(this.getLastError());
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void nameService(String name, int afCmd) throws DdemlException {
            Ddeml.HSZ nameHSZ = null;
            try {
                nameHSZ = this.createStringHandle(name);
                this.nameService(nameHSZ, afCmd);
            }
            finally {
                this.freeStringHandle(nameHSZ);
            }
        }

        @Override
        public int getLastError() {
            return Ddeml.INSTANCE.DdeGetLastError(this.idInst);
        }

        @Override
        public IDdeConnection connect(Ddeml.HSZ service, Ddeml.HSZ topic, Ddeml.CONVCONTEXT convcontext) {
            Ddeml.HCONV hconv = Ddeml.INSTANCE.DdeConnect(this.idInst, service, topic, convcontext);
            if (hconv == null) {
                throw DdemlException.create(this.getLastError());
            }
            return new DdeConnection(this, hconv);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public IDdeConnection connect(String service, String topic, Ddeml.CONVCONTEXT convcontext) {
            IDdeConnection iDdeConnection;
            Ddeml.HSZ serviceHSZ = null;
            Ddeml.HSZ topicHSZ = null;
            try {
                serviceHSZ = this.createStringHandle(service);
                topicHSZ = this.createStringHandle(topic);
                iDdeConnection = this.connect(serviceHSZ, topicHSZ, convcontext);
                this.freeStringHandle(topicHSZ);
            }
            catch (Throwable throwable) {
                this.freeStringHandle(topicHSZ);
                this.freeStringHandle(serviceHSZ);
                throw throwable;
            }
            this.freeStringHandle(serviceHSZ);
            return iDdeConnection;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String queryString(Ddeml.HSZ value) throws DdemlException {
            int byteWidth;
            int codePage;
            if (W32APIOptions.DEFAULT_OPTIONS == W32APIOptions.UNICODE_OPTIONS) {
                codePage = 1200;
                byteWidth = 2;
            } else {
                codePage = 1004;
                byteWidth = 1;
            }
            Memory buffer = new Memory(257 * byteWidth);
            try {
                int length = Ddeml.INSTANCE.DdeQueryString(this.idInst, value, buffer, 256, codePage);
                if (W32APIOptions.DEFAULT_OPTIONS == W32APIOptions.UNICODE_OPTIONS) {
                    String string = buffer.getWideString(0L);
                    return string;
                }
                String string = buffer.getString(0L);
                return string;
            }
            finally {
                buffer.valid();
            }
        }

        @Override
        public Ddeml.HDDEDATA createDataHandle(Pointer pSrc, int cb, int cbOff, Ddeml.HSZ hszItem, int wFmt, int afCmd) {
            Ddeml.HDDEDATA returnData = Ddeml.INSTANCE.DdeCreateDataHandle(this.idInst, pSrc, cb, cbOff, hszItem, wFmt, afCmd);
            if (returnData == null) {
                throw DdemlException.create(this.getLastError());
            }
            return returnData;
        }

        @Override
        public void freeDataHandle(Ddeml.HDDEDATA hData) {
            boolean result = Ddeml.INSTANCE.DdeFreeDataHandle(hData);
            if (!result) {
                throw DdemlException.create(this.getLastError());
            }
        }

        @Override
        public Ddeml.HDDEDATA addData(Ddeml.HDDEDATA hData, Pointer pSrc, int cb, int cbOff) {
            Ddeml.HDDEDATA newHandle = Ddeml.INSTANCE.DdeAddData(hData, pSrc, cb, cbOff);
            if (newHandle == null) {
                throw DdemlException.create(this.getLastError());
            }
            return newHandle;
        }

        @Override
        public int getData(Ddeml.HDDEDATA hData, Pointer pDst, int cbMax, int cbOff) {
            int result = Ddeml.INSTANCE.DdeGetData(hData, pDst, cbMax, cbOff);
            int errorCode = this.getLastError();
            if (errorCode != 0) {
                throw DdemlException.create(errorCode);
            }
            return result;
        }

        @Override
        public Pointer accessData(Ddeml.HDDEDATA hData, WinDef.DWORDByReference pcbDataSize) {
            Pointer result = Ddeml.INSTANCE.DdeAccessData(hData, pcbDataSize);
            if (result == null) {
                throw DdemlException.create(this.getLastError());
            }
            return result;
        }

        @Override
        public void unaccessData(Ddeml.HDDEDATA hData) {
            boolean result = Ddeml.INSTANCE.DdeUnaccessData(hData);
            if (!result) {
                throw DdemlException.create(this.getLastError());
            }
        }

        @Override
        public void postAdvise(Ddeml.HSZ hszTopic, Ddeml.HSZ hszItem) {
            boolean result = Ddeml.INSTANCE.DdePostAdvise(this.idInst, hszTopic, hszItem);
            if (!result) {
                throw DdemlException.create(this.getLastError());
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void postAdvise(String topic, String item) {
            Ddeml.HSZ itemHSZ = null;
            Ddeml.HSZ topicHSZ = null;
            try {
                topicHSZ = this.createStringHandle(topic);
                itemHSZ = this.createStringHandle(item);
                this.postAdvise(topicHSZ, itemHSZ);
            }
            finally {
                this.freeStringHandle(topicHSZ);
                this.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public boolean freeStringHandle(Ddeml.HSZ value) {
            if (value == null) {
                return true;
            }
            return Ddeml.INSTANCE.DdeFreeStringHandle(this.idInst, value);
        }

        @Override
        public boolean keepStringHandle(Ddeml.HSZ value) {
            return Ddeml.INSTANCE.DdeKeepStringHandle(this.idInst, value);
        }

        @Override
        public void abandonTransactions() {
            boolean result = Ddeml.INSTANCE.DdeAbandonTransaction(this.idInst, null, 0);
            if (!result) {
                throw DdemlException.create(this.getLastError());
            }
        }

        @Override
        public IDdeConnectionList connectList(Ddeml.HSZ service, Ddeml.HSZ topic, IDdeConnectionList existingList, Ddeml.CONVCONTEXT ctx) {
            Ddeml.HCONVLIST convlist = Ddeml.INSTANCE.DdeConnectList(this.idInst, service, topic, existingList != null ? existingList.getHandle() : null, ctx);
            if (convlist == null) {
                throw DdemlException.create(this.getLastError());
            }
            return new DdeConnectionList(this, convlist);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public IDdeConnectionList connectList(String service, String topic, IDdeConnectionList existingList, Ddeml.CONVCONTEXT ctx) {
            IDdeConnectionList iDdeConnectionList;
            Ddeml.HSZ serviceHSZ = null;
            Ddeml.HSZ topicHSZ = null;
            try {
                serviceHSZ = this.createStringHandle(service);
                topicHSZ = this.createStringHandle(topic);
                iDdeConnectionList = this.connectList(serviceHSZ, topicHSZ, existingList, ctx);
                this.freeStringHandle(topicHSZ);
            }
            catch (Throwable throwable) {
                this.freeStringHandle(topicHSZ);
                this.freeStringHandle(serviceHSZ);
                throw throwable;
            }
            this.freeStringHandle(serviceHSZ);
            return iDdeConnectionList;
        }

        @Override
        public boolean enableCallback(int wCmd) {
            int errorCode;
            boolean result = Ddeml.INSTANCE.DdeEnableCallback(this.idInst, null, wCmd);
            if (!result && wCmd != 2 && (errorCode = this.getLastError()) != 0) {
                throw DdemlException.create(this.getLastError());
            }
            return result;
        }

        @Override
        public boolean uninitialize() {
            return Ddeml.INSTANCE.DdeUninitialize(this.idInst);
        }

        @Override
        public void close() {
            this.uninitialize();
        }

        @Override
        public IDdeConnection wrap(Ddeml.HCONV hconv) {
            return new DdeConnection(this, hconv);
        }

        @Override
        public void unregisterDisconnectHandler(DisconnectHandler handler) {
            this.ddeAdapter.unregisterDisconnectHandler(handler);
        }

        @Override
        public void registerAdvstartHandler(AdvstartHandler handler) {
            this.ddeAdapter.registerAdvstartHandler(handler);
        }

        @Override
        public void unregisterAdvstartHandler(AdvstartHandler handler) {
            this.ddeAdapter.unregisterAdvstartHandler(handler);
        }

        @Override
        public void registerAdvstopHandler(AdvstopHandler handler) {
            this.ddeAdapter.registerAdvstopHandler(handler);
        }

        @Override
        public void unregisterAdvstopHandler(AdvstopHandler handler) {
            this.ddeAdapter.unregisterAdvstopHandler(handler);
        }

        @Override
        public void registerConnectHandler(ConnectHandler handler) {
            this.ddeAdapter.registerConnectHandler(handler);
        }

        @Override
        public void unregisterConnectHandler(ConnectHandler handler) {
            this.ddeAdapter.unregisterConnectHandler(handler);
        }

        @Override
        public void registerAdvReqHandler(AdvreqHandler handler) {
            this.ddeAdapter.registerAdvReqHandler(handler);
        }

        @Override
        public void unregisterAdvReqHandler(AdvreqHandler handler) {
            this.ddeAdapter.unregisterAdvReqHandler(handler);
        }

        @Override
        public void registerRequestHandler(RequestHandler handler) {
            this.ddeAdapter.registerRequestHandler(handler);
        }

        @Override
        public void unregisterRequestHandler(RequestHandler handler) {
            this.ddeAdapter.unregisterRequestHandler(handler);
        }

        @Override
        public void registerWildconnectHandler(WildconnectHandler handler) {
            this.ddeAdapter.registerWildconnectHandler(handler);
        }

        @Override
        public void unregisterWildconnectHandler(WildconnectHandler handler) {
            this.ddeAdapter.unregisterWildconnectHandler(handler);
        }

        @Override
        public void registerAdvdataHandler(AdvdataHandler handler) {
            this.ddeAdapter.registerAdvdataHandler(handler);
        }

        @Override
        public void unregisterAdvdataHandler(AdvdataHandler handler) {
            this.ddeAdapter.unregisterAdvdataHandler(handler);
        }

        @Override
        public void registerExecuteHandler(ExecuteHandler handler) {
            this.ddeAdapter.registerExecuteHandler(handler);
        }

        @Override
        public void unregisterExecuteHandler(ExecuteHandler handler) {
            this.ddeAdapter.unregisterExecuteHandler(handler);
        }

        @Override
        public void registerPokeHandler(PokeHandler handler) {
            this.ddeAdapter.registerPokeHandler(handler);
        }

        @Override
        public void unregisterPokeHandler(PokeHandler handler) {
            this.ddeAdapter.unregisterPokeHandler(handler);
        }

        @Override
        public void registerConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.ddeAdapter.registerConnectConfirmHandler(handler);
        }

        @Override
        public void unregisterConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.ddeAdapter.unregisterConnectConfirmHandler(handler);
        }

        @Override
        public void registerDisconnectHandler(DisconnectHandler handler) {
            this.ddeAdapter.registerDisconnectHandler(handler);
        }

        @Override
        public void registerErrorHandler(ErrorHandler handler) {
            this.ddeAdapter.registerErrorHandler(handler);
        }

        @Override
        public void unregisterErrorHandler(ErrorHandler handler) {
            this.ddeAdapter.unregisterErrorHandler(handler);
        }

        @Override
        public void registerRegisterHandler(RegisterHandler handler) {
            this.ddeAdapter.registerRegisterHandler(handler);
        }

        @Override
        public void unregisterRegisterHandler(RegisterHandler handler) {
            this.ddeAdapter.unregisterRegisterHandler(handler);
        }

        @Override
        public void registerXactCompleteHandler(XactCompleteHandler handler) {
            this.ddeAdapter.registerXactCompleteHandler(handler);
        }

        @Override
        public void unregisterXactCompleteHandler(XactCompleteHandler handler) {
            this.ddeAdapter.xactCompleteXactCompleteHandler(handler);
        }

        @Override
        public void registerUnregisterHandler(UnregisterHandler handler) {
            this.ddeAdapter.registerUnregisterHandler(handler);
        }

        @Override
        public void unregisterUnregisterHandler(UnregisterHandler handler) {
            this.ddeAdapter.unregisterUnregisterHandler(handler);
        }

        @Override
        public void registerMonitorHandler(MonitorHandler handler) {
            this.ddeAdapter.registerMonitorHandler(handler);
        }

        @Override
        public void unregisterMonitorHandler(MonitorHandler handler) {
            this.ddeAdapter.unregisterMonitorHandler(handler);
        }
    }

    public static class DdeConnectionList
    implements IDdeConnectionList {
        private final IDdeClient client;
        private final Ddeml.HCONVLIST convList;

        public DdeConnectionList(IDdeClient client, Ddeml.HCONVLIST convList) {
            this.convList = convList;
            this.client = client;
        }

        @Override
        public Ddeml.HCONVLIST getHandle() {
            return this.convList;
        }

        @Override
        public IDdeConnection queryNextServer(IDdeConnection prevConnection) {
            Ddeml.HCONV conv = Ddeml.INSTANCE.DdeQueryNextServer(this.convList, prevConnection != null ? prevConnection.getConv() : null);
            if (conv != null) {
                return new DdeConnection(this.client, conv);
            }
            return null;
        }

        @Override
        public void close() {
            boolean result = Ddeml.INSTANCE.DdeDisconnectList(this.convList);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }
    }

    public static class DdeConnection
    implements IDdeConnection {
        private Ddeml.HCONV conv;
        private final IDdeClient client;

        public DdeConnection(IDdeClient client, Ddeml.HCONV conv) {
            this.conv = conv;
            this.client = client;
        }

        @Override
        public Ddeml.HCONV getConv() {
            return this.conv;
        }

        @Override
        public void abandonTransaction(int transactionId) {
            boolean result = Ddeml.INSTANCE.DdeAbandonTransaction(this.client.getInstanceIdentitifier(), this.conv, transactionId);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }

        @Override
        public void abandonTransactions() {
            boolean result = Ddeml.INSTANCE.DdeAbandonTransaction(this.client.getInstanceIdentitifier(), this.conv, 0);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }

        @Override
        public Ddeml.HDDEDATA clientTransaction(Pointer data, int dataLength, Ddeml.HSZ item, int wFmt, int transaction, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HDDEDATA returnData;
            if (timeout2 == -1 && result == null) {
                result = new WinDef.DWORDByReference();
            }
            if ((returnData = Ddeml.INSTANCE.DdeClientTransaction(data, dataLength, this.conv, item, wFmt, transaction, timeout2, result)) == null) {
                throw DdemlException.create(this.client.getLastError());
            }
            if (userHandle != null) {
                if (timeout2 != -1) {
                    this.setUserHandle(-1, userHandle);
                } else {
                    this.setUserHandle(result.getValue().intValue(), userHandle);
                }
            }
            return returnData;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Ddeml.HDDEDATA clientTransaction(Pointer data, int dataLength, String item, int wFmt, int transaction, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HSZ itemHSZ = null;
            try {
                itemHSZ = this.client.createStringHandle(item);
                Ddeml.HDDEDATA hDDEDATA = this.clientTransaction(data, dataLength, itemHSZ, wFmt, transaction, timeout2, result, userHandle);
                return hDDEDATA;
            }
            finally {
                this.client.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public void poke(Pointer data, int dataLength, Ddeml.HSZ item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            this.clientTransaction(data, dataLength, item, wFmt, 16528, timeout2, result, userHandle);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void poke(Pointer data, int dataLength, String item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HSZ itemHSZ = null;
            try {
                itemHSZ = this.client.createStringHandle(item);
                this.poke(data, dataLength, itemHSZ, wFmt, timeout2, result, userHandle);
            }
            finally {
                this.client.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public Ddeml.HDDEDATA request(Ddeml.HSZ item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            return this.clientTransaction(Pointer.NULL, 0, item, wFmt, 8368, timeout2, result, userHandle);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Ddeml.HDDEDATA request(String item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HSZ itemHSZ = null;
            try {
                itemHSZ = this.client.createStringHandle(item);
                Ddeml.HDDEDATA hDDEDATA = this.request(itemHSZ, wFmt, timeout2, result, userHandle);
                return hDDEDATA;
            }
            finally {
                this.client.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public void execute(String executeString, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Memory mem = new Memory(executeString.length() * 2 + 2);
            mem.setWideString(0L, executeString);
            this.clientTransaction((Pointer)mem, (int)mem.size(), (Ddeml.HSZ)null, 0, 16464, timeout2, result, userHandle);
        }

        @Override
        public void advstart(Ddeml.HSZ item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            this.clientTransaction(Pointer.NULL, 0, item, wFmt, 4144, timeout2, result, userHandle);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void advstart(String item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HSZ itemHSZ = null;
            try {
                itemHSZ = this.client.createStringHandle(item);
                this.advstart(itemHSZ, wFmt, timeout2, result, userHandle);
            }
            finally {
                this.client.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public void advstop(Ddeml.HSZ item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            this.clientTransaction(Pointer.NULL, 0, item, wFmt, 32832, timeout2, result, userHandle);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void advstop(String item, int wFmt, int timeout2, WinDef.DWORDByReference result, BaseTSD.DWORD_PTR userHandle) {
            Ddeml.HSZ itemHSZ = null;
            try {
                itemHSZ = this.client.createStringHandle(item);
                this.advstop(itemHSZ, wFmt, timeout2, result, userHandle);
            }
            finally {
                this.client.freeStringHandle(itemHSZ);
            }
        }

        @Override
        public void impersonateClient() {
            boolean result = Ddeml.INSTANCE.DdeImpersonateClient(this.conv);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }

        @Override
        public void close() {
            boolean result = Ddeml.INSTANCE.DdeDisconnect(this.conv);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }

        @Override
        public void reconnect() {
            Ddeml.HCONV newConv = Ddeml.INSTANCE.DdeReconnect(this.conv);
            if (newConv == null) {
                throw DdemlException.create(this.client.getLastError());
            }
            this.conv = newConv;
        }

        @Override
        public boolean enableCallback(int wCmd) {
            boolean result = Ddeml.INSTANCE.DdeEnableCallback(this.client.getInstanceIdentitifier(), this.conv, wCmd);
            if (!result && wCmd == 2) {
                throw DdemlException.create(this.client.getLastError());
            }
            return result;
        }

        @Override
        public void setUserHandle(int id, BaseTSD.DWORD_PTR hUser) throws DdemlException {
            boolean result = Ddeml.INSTANCE.DdeSetUserHandle(this.conv, id, hUser);
            if (!result) {
                throw DdemlException.create(this.client.getLastError());
            }
        }

        @Override
        public Ddeml.CONVINFO queryConvInfo(int idTransaction) throws DdemlException {
            Ddeml.CONVINFO convInfo = new Ddeml.CONVINFO();
            convInfo.cb = convInfo.size();
            convInfo.ConvCtxt.cb = convInfo.ConvCtxt.size();
            convInfo.write();
            int result = Ddeml.INSTANCE.DdeQueryConvInfo(this.conv, idTransaction, convInfo);
            if (result == 0) {
                throw DdemlException.create(this.client.getLastError());
            }
            return convInfo;
        }
    }

    private static class MessageLoopWrapper
    implements InvocationHandler {
        private final Object delegate;
        private final User32Util.MessageLoopThread loopThread;

        public MessageLoopWrapper(User32Util.MessageLoopThread thread2, Object delegate) {
            this.loopThread = thread2;
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args2) throws Throwable {
            try {
                Object result = method.invoke(this.delegate, args2);
                Class wrapClass = null;
                if (result instanceof IDdeConnection) {
                    wrapClass = IDdeConnection.class;
                } else if (result instanceof IDdeConnectionList) {
                    wrapClass = IDdeConnectionList.class;
                } else if (result instanceof IDdeClient) {
                    wrapClass = IDdeClient.class;
                }
                if (wrapClass != null && method.getReturnType().isAssignableFrom(wrapClass)) {
                    result = this.wrap(result, wrapClass);
                }
                return result;
            }
            catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception) {
                    throw (Exception)cause;
                }
                throw ex;
            }
        }

        private <V> V wrap(V delegate, Class clazz) {
            ClassLoader classLoader = StandaloneDdeClient.class.getClassLoader();
            Class[] classArray = new Class[]{clazz};
            User32Util.MessageLoopThread messageLoopThread = this.loopThread;
            Objects.requireNonNull(messageLoopThread);
            Object messageLoopHandler = Proxy.newProxyInstance(classLoader, classArray, new User32Util.MessageLoopThread.Handler(messageLoopThread, delegate));
            Object clientDelegate = Proxy.newProxyInstance(StandaloneDdeClient.class.getClassLoader(), new Class[]{clazz}, new MessageLoopWrapper(this.loopThread, messageLoopHandler));
            return (V)clientDelegate;
        }
    }

    public static class StandaloneDdeClient
    implements IDdeClient,
    Closeable {
        private final User32Util.MessageLoopThread messageLoop = new User32Util.MessageLoopThread();
        private final IDdeClient ddeClient = new DdeClient();
        private final IDdeClient clientDelegate;

        public StandaloneDdeClient() {
            ClassLoader classLoader = StandaloneDdeClient.class.getClassLoader();
            Class[] classArray = new Class[]{IDdeClient.class};
            User32Util.MessageLoopThread messageLoopThread = this.messageLoop;
            Objects.requireNonNull(messageLoopThread);
            IDdeClient messageLoopHandler = (IDdeClient)Proxy.newProxyInstance(classLoader, classArray, new User32Util.MessageLoopThread.Handler(messageLoopThread, this.ddeClient));
            this.clientDelegate = (IDdeClient)Proxy.newProxyInstance(StandaloneDdeClient.class.getClassLoader(), new Class[]{IDdeClient.class}, new MessageLoopWrapper(this.messageLoop, messageLoopHandler));
            this.messageLoop.setDaemon(true);
            this.messageLoop.start();
        }

        @Override
        public Integer getInstanceIdentitifier() {
            return this.ddeClient.getInstanceIdentitifier();
        }

        @Override
        public void initialize(int afCmd) throws DdemlException {
            this.clientDelegate.initialize(afCmd);
        }

        @Override
        public Ddeml.HSZ createStringHandle(String value) throws DdemlException {
            return this.clientDelegate.createStringHandle(value);
        }

        @Override
        public void nameService(Ddeml.HSZ name, int afCmd) throws DdemlException {
            this.clientDelegate.nameService(name, afCmd);
        }

        @Override
        public int getLastError() {
            return this.clientDelegate.getLastError();
        }

        @Override
        public IDdeConnection connect(Ddeml.HSZ service, Ddeml.HSZ topic, Ddeml.CONVCONTEXT convcontext) {
            return this.clientDelegate.connect(service, topic, convcontext);
        }

        @Override
        public String queryString(Ddeml.HSZ value) throws DdemlException {
            return this.clientDelegate.queryString(value);
        }

        @Override
        public Ddeml.HDDEDATA createDataHandle(Pointer pSrc, int cb, int cbOff, Ddeml.HSZ hszItem, int wFmt, int afCmd) {
            return this.clientDelegate.createDataHandle(pSrc, cb, cbOff, hszItem, wFmt, afCmd);
        }

        @Override
        public void freeDataHandle(Ddeml.HDDEDATA hData) {
            this.clientDelegate.freeDataHandle(hData);
        }

        @Override
        public Ddeml.HDDEDATA addData(Ddeml.HDDEDATA hData, Pointer pSrc, int cb, int cbOff) {
            return this.clientDelegate.addData(hData, pSrc, cb, cbOff);
        }

        @Override
        public int getData(Ddeml.HDDEDATA hData, Pointer pDst, int cbMax, int cbOff) {
            return this.clientDelegate.getData(hData, pDst, cbMax, cbOff);
        }

        @Override
        public Pointer accessData(Ddeml.HDDEDATA hData, WinDef.DWORDByReference pcbDataSize) {
            return this.clientDelegate.accessData(hData, pcbDataSize);
        }

        @Override
        public void unaccessData(Ddeml.HDDEDATA hData) {
            this.clientDelegate.unaccessData(hData);
        }

        @Override
        public void postAdvise(Ddeml.HSZ hszTopic, Ddeml.HSZ hszItem) {
            this.clientDelegate.postAdvise(hszTopic, hszItem);
        }

        @Override
        public void close() throws IOException {
            this.clientDelegate.uninitialize();
            this.messageLoop.exit();
        }

        @Override
        public boolean freeStringHandle(Ddeml.HSZ value) {
            return this.clientDelegate.freeStringHandle(value);
        }

        @Override
        public boolean keepStringHandle(Ddeml.HSZ value) {
            return this.clientDelegate.keepStringHandle(value);
        }

        @Override
        public void abandonTransactions() {
            this.clientDelegate.abandonTransactions();
        }

        @Override
        public IDdeConnectionList connectList(Ddeml.HSZ service, Ddeml.HSZ topic, IDdeConnectionList existingList, Ddeml.CONVCONTEXT ctx) {
            return this.clientDelegate.connectList(service, topic, existingList, ctx);
        }

        @Override
        public boolean enableCallback(int wCmd) {
            return this.clientDelegate.enableCallback(wCmd);
        }

        @Override
        public IDdeConnection wrap(Ddeml.HCONV conv) {
            return this.clientDelegate.wrap(conv);
        }

        @Override
        public IDdeConnection connect(String service, String topic, Ddeml.CONVCONTEXT convcontext) {
            return this.clientDelegate.connect(service, topic, convcontext);
        }

        @Override
        public boolean uninitialize() {
            return this.clientDelegate.uninitialize();
        }

        @Override
        public void postAdvise(String hszTopic, String hszItem) {
            this.clientDelegate.postAdvise(hszTopic, hszItem);
        }

        @Override
        public IDdeConnectionList connectList(String service, String topic, IDdeConnectionList existingList, Ddeml.CONVCONTEXT ctx) {
            return this.clientDelegate.connectList(service, topic, existingList, ctx);
        }

        @Override
        public void nameService(String name, int afCmd) throws DdemlException {
            this.clientDelegate.nameService(name, afCmd);
        }

        @Override
        public void registerAdvstartHandler(AdvstartHandler handler) {
            this.clientDelegate.registerAdvstartHandler(handler);
        }

        @Override
        public void unregisterAdvstartHandler(AdvstartHandler handler) {
            this.clientDelegate.unregisterAdvstartHandler(handler);
        }

        @Override
        public void registerAdvstopHandler(AdvstopHandler handler) {
            this.clientDelegate.registerAdvstopHandler(handler);
        }

        @Override
        public void unregisterAdvstopHandler(AdvstopHandler handler) {
            this.clientDelegate.unregisterAdvstopHandler(handler);
        }

        @Override
        public void registerConnectHandler(ConnectHandler handler) {
            this.clientDelegate.registerConnectHandler(handler);
        }

        @Override
        public void unregisterConnectHandler(ConnectHandler handler) {
            this.clientDelegate.unregisterConnectHandler(handler);
        }

        @Override
        public void registerAdvReqHandler(AdvreqHandler handler) {
            this.clientDelegate.registerAdvReqHandler(handler);
        }

        @Override
        public void unregisterAdvReqHandler(AdvreqHandler handler) {
            this.clientDelegate.unregisterAdvReqHandler(handler);
        }

        @Override
        public void registerRequestHandler(RequestHandler handler) {
            this.clientDelegate.registerRequestHandler(handler);
        }

        @Override
        public void unregisterRequestHandler(RequestHandler handler) {
            this.clientDelegate.unregisterRequestHandler(handler);
        }

        @Override
        public void registerWildconnectHandler(WildconnectHandler handler) {
            this.clientDelegate.registerWildconnectHandler(handler);
        }

        @Override
        public void unregisterWildconnectHandler(WildconnectHandler handler) {
            this.clientDelegate.unregisterWildconnectHandler(handler);
        }

        @Override
        public void registerAdvdataHandler(AdvdataHandler handler) {
            this.clientDelegate.registerAdvdataHandler(handler);
        }

        @Override
        public void unregisterAdvdataHandler(AdvdataHandler handler) {
            this.clientDelegate.unregisterAdvdataHandler(handler);
        }

        @Override
        public void registerExecuteHandler(ExecuteHandler handler) {
            this.clientDelegate.registerExecuteHandler(handler);
        }

        @Override
        public void unregisterExecuteHandler(ExecuteHandler handler) {
            this.clientDelegate.unregisterExecuteHandler(handler);
        }

        @Override
        public void registerPokeHandler(PokeHandler handler) {
            this.clientDelegate.registerPokeHandler(handler);
        }

        @Override
        public void unregisterPokeHandler(PokeHandler handler) {
            this.clientDelegate.unregisterPokeHandler(handler);
        }

        @Override
        public void registerConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.clientDelegate.registerConnectConfirmHandler(handler);
        }

        @Override
        public void unregisterConnectConfirmHandler(ConnectConfirmHandler handler) {
            this.clientDelegate.unregisterConnectConfirmHandler(handler);
        }

        @Override
        public void registerDisconnectHandler(DisconnectHandler handler) {
            this.clientDelegate.registerDisconnectHandler(handler);
        }

        @Override
        public void unregisterDisconnectHandler(DisconnectHandler handler) {
            this.clientDelegate.unregisterDisconnectHandler(handler);
        }

        @Override
        public void registerErrorHandler(ErrorHandler handler) {
            this.clientDelegate.registerErrorHandler(handler);
        }

        @Override
        public void unregisterErrorHandler(ErrorHandler handler) {
            this.clientDelegate.unregisterErrorHandler(handler);
        }

        @Override
        public void registerRegisterHandler(RegisterHandler handler) {
            this.clientDelegate.registerRegisterHandler(handler);
        }

        @Override
        public void unregisterRegisterHandler(RegisterHandler handler) {
            this.clientDelegate.unregisterRegisterHandler(handler);
        }

        @Override
        public void registerXactCompleteHandler(XactCompleteHandler handler) {
            this.clientDelegate.registerXactCompleteHandler(handler);
        }

        @Override
        public void unregisterXactCompleteHandler(XactCompleteHandler handler) {
            this.clientDelegate.unregisterXactCompleteHandler(handler);
        }

        @Override
        public void registerUnregisterHandler(UnregisterHandler handler) {
            this.clientDelegate.registerUnregisterHandler(handler);
        }

        @Override
        public void unregisterUnregisterHandler(UnregisterHandler handler) {
            this.clientDelegate.unregisterUnregisterHandler(handler);
        }

        @Override
        public void registerMonitorHandler(MonitorHandler handler) {
            this.clientDelegate.registerMonitorHandler(handler);
        }

        @Override
        public void unregisterMonitorHandler(MonitorHandler handler) {
            this.clientDelegate.unregisterMonitorHandler(handler);
        }
    }
}

