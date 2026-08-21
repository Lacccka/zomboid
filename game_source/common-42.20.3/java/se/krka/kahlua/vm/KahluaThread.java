/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import se.krka.kahlua.vm.Prototype;
import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.logger.ExceptionLogger;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.debug.StackTraceContainer;
import zombie.network.GameServer;
import zombie.ui.UIManager;

public class KahluaThread {
    private static final int FIELDS_PER_FLUSH = 50;
    private static final int OP_MOVE = 0;
    private static final int OP_LOADK = 1;
    private static final int OP_LOADBOOL = 2;
    private static final int OP_LOADNIL = 3;
    private static final int OP_GETUPVAL = 4;
    private static final int OP_GETGLOBAL = 5;
    private static final int OP_GETTABLE = 6;
    private static final int OP_SETGLOBAL = 7;
    private static final int OP_SETUPVAL = 8;
    private static final int OP_SETTABLE = 9;
    private static final int OP_NEWTABLE = 10;
    private static final int OP_SELF = 11;
    private static final int OP_ADD = 12;
    private static final int OP_SUB = 13;
    private static final int OP_MUL = 14;
    private static final int OP_DIV = 15;
    private static final int OP_MOD = 16;
    private static final int OP_POW = 17;
    private static final int OP_UNM = 18;
    private static final int OP_NOT = 19;
    private static final int OP_LEN = 20;
    private static final int OP_CONCAT = 21;
    private static final int OP_JMP = 22;
    private static final int OP_EQ = 23;
    private static final int OP_LT = 24;
    private static final int OP_LE = 25;
    private static final int OP_TEST = 26;
    private static final int OP_TESTSET = 27;
    private static final int OP_CALL = 28;
    private static final int OP_TAILCALL = 29;
    private static final int OP_RETURN = 30;
    private static final int OP_FORLOOP = 31;
    private static final int OP_FORPREP = 32;
    private static final int OP_TFORLOOP = 33;
    private static final int OP_SETLIST = 34;
    private static final int OP_CLOSE = 35;
    private static final int OP_CLOSURE = 36;
    private static final int OP_VARARG = 37;
    private static final int MAX_INDEX_RECURSION = 100;
    private static final String[] meta_ops = new String[38];
    public static LuaCallFrame lastCallFrame;
    private final Coroutine rootCoroutine;
    private final Map<Class<?>, KahluaTable> cachedMetatables = new IdentityHashMap();
    public Coroutine currentCoroutine;
    private final boolean doProfiling = false;
    public Thread debugOwnerThread;
    private final PrintStream out;
    private final Platform platform;
    private final StringBuilder concatBuffer = new StringBuilder();
    public boolean step;
    public String currentfile;
    public int currentLine;
    public int lastLine;
    public int lastCallFrameIdx;
    public boolean reset;
    public ArrayList<Entry> profileEntries = new ArrayList();
    public HashMap<String, Entry> profileEntryMap = new HashMap();
    private static final String CLASS_NAME;
    private static final String METHOD_NAME_LUA_MAINLOOP = "luaMainloop";
    public static int errorCount;
    public static final ArrayList<String> m_errors_list;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(this.stringWriter);
    HashMap<String, ArrayList<Long>> breakpointMap = new HashMap();
    HashMap<KahluaTable, ArrayList<Object>> breakpointDataMap = new HashMap();
    HashMap<KahluaTable, ArrayList<Object>> breakpointReadDataMap = new HashMap();
    public boolean stepInto;

    public Coroutine getCurrentCoroutine() {
        return this.currentCoroutine;
    }

    public KahluaThread(Platform platform, KahluaTable environment) {
        this(null, platform, environment);
    }

    public KahluaThread(PrintStream stream, Platform platform, KahluaTable environment) {
        this.platform = platform;
        this.out = stream != null ? stream : DebugType.Lua.getLogStream();
        this.currentCoroutine = this.rootCoroutine = new Coroutine(platform, environment, this);
    }

    public int call(int nArguments) {
        int top = this.currentCoroutine.getTop();
        int base = top - nArguments - 1;
        Object o = this.currentCoroutine.objectStack[base];
        if (o == null) {
            throw new RuntimeException("tried to call nil");
        }
        try {
            if (o instanceof JavaFunction) {
                JavaFunction javaFunction = (JavaFunction)o;
                return this.callJava(javaFunction, base + 1, base, nArguments);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex.getClass().getName() + " " + ex.getMessage() + " in " + String.valueOf(o));
        }
        if (!(o instanceof LuaClosure)) {
            throw new RuntimeException("tried to call a non-function");
        }
        LuaClosure luaClosure = (LuaClosure)o;
        LuaCallFrame callFrame = this.currentCoroutine.pushNewCallFrame(luaClosure, null, base + 1, base, nArguments, false, false);
        callFrame.init();
        this.luaMainloop();
        int nReturnValues = this.currentCoroutine.getTop() - base;
        this.currentCoroutine.stackTrace = "";
        return nReturnValues;
    }

    private int callJava(JavaFunction f, int localBase, int returnBase, int nArguments) {
        Coroutine coroutine = this.currentCoroutine;
        LuaCallFrame callFrame = coroutine.pushNewCallFrame(null, f, localBase, returnBase, nArguments, false, false);
        int nReturnValues = f.call(callFrame, nArguments);
        int top = callFrame.getTop();
        int actualReturnBase = top - nReturnValues;
        int diff = returnBase - localBase;
        callFrame.stackCopy(actualReturnBase, diff, nReturnValues);
        callFrame.setTop(nReturnValues + diff);
        coroutine.popCallFrame();
        return nReturnValues;
    }

    private final Object prepareMetatableCall(Object o) {
        if (o instanceof JavaFunction || o instanceof LuaClosure) {
            return o;
        }
        Object f = this.getMetaOp(o, "__call");
        return f;
    }

    public boolean isCurrent(String file, int line) {
        return line == this.currentLine;
    }

    public static boolean isLuaMainloop(StackTraceElement stackTraceElement) {
        return stackTraceElement != null && CLASS_NAME.equals(stackTraceElement.getClassName()) && METHOD_NAME_LUA_MAINLOOP.equals(stackTraceElement.getMethodName());
    }

    /*
     * Unable to fully structure code
     */
    private final void luaMainloop() {
        callFrame = this.currentCoroutine.currentCallFrame();
        closure = callFrame.closure;
        prototype = closure.prototype;
        opcodes = prototype.code;
        returnBase = callFrame.returnBase;
        file = "";
        start = 0L;
        bStepWorthy = true;
        while (!this.reset) {
            block148: {
                if (Core.debug && this == LuaManager.thread && (c = this.getCurrentCoroutine()) != null) {
                    this.lastLine = this.currentLine;
                    f = c.currentCallFrame();
                    if (f.closure != null) {
                        this.currentfile = f.closure.prototype.filename;
                        this.currentLine = f.closure.prototype.lines[f.pc];
                        if (this.step && this.currentLine != this.lastLine) {
                            if (this.stepInto) {
                                this.step = false;
                                UIManager.debugBreakpoint(f.closure.prototype.filename, (long)this.currentLine - 1L);
                                this.lastCallFrameIdx = c.getCallframeTop();
                                bStepWorthy = true;
                            } else if (c.getCallframeTop() <= this.lastCallFrameIdx) {
                                this.step = false;
                                this.lastCallFrameIdx = c.getCallframeTop();
                                UIManager.debugBreakpoint(f.closure.prototype.filename, (long)this.currentLine - 1L);
                                bStepWorthy = true;
                            }
                        }
                        if (this.breakpointMap.containsKey(f.closure.prototype.filename) && (i = this.breakpointMap.get(f.closure.prototype.filename)).contains(f.closure.prototype.lines[f.pc]) && (f.pc == 0 || f.closure.prototype.lines[f.pc - 1] != f.closure.prototype.lines[f.pc])) {
                            UIManager.debugBreakpoint(f.closure.prototype.filename, f.closure.prototype.lines[f.pc]);
                        }
                    }
                }
                bStepWorthy = true;
                try {
                    if (this.step) {
                        var13_82 = false;
                    }
                    op = opcodes[callFrame.pc++];
                    opcode = op & 63;
                    switch (opcode) {
                        case 0: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            callFrame.set(a, callFrame.get(b));
                            bStepWorthy = false;
                            break;
                        }
                        case 1: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getBx(op);
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, prototype.constants[b]);
                            break;
                        }
                        case 2: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            v0 = bool = b == 0 ? Boolean.FALSE : Boolean.TRUE;
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, bool);
                            if (c == 0) break;
                            ++callFrame.pc;
                            break;
                        }
                        case 3: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.stackClear(a, b);
                            break;
                        }
                        case 4: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            uv = closure.upvalues[b];
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, uv.getValue());
                            break;
                        }
                        case 5: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getBx(op);
                            res = this.tableget(closure.env, prototype.constants[b]);
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, res);
                            break;
                        }
                        case 6: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            bObj = callFrame.get(b);
                            key = this.getRegisterOrConstant(callFrame, c, prototype);
                            res = this.tableget(bObj, key);
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, res);
                            break;
                        }
                        case 7: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getBx(op);
                            value = callFrame.get(a);
                            key = prototype.constants[b];
                            if (value instanceof LuaClosure) {
                                luaClosure = (LuaClosure)value;
                                if (key instanceof String) {
                                    luaClosure.debugName = key.toString();
                                }
                            }
                            if (LuaCompiler.rewriteEvents) {
                                o = closure.env.rawget(key);
                                if (o instanceof KahluaTable && o != value) {
                                    tab = (KahluaTableImpl)o;
                                    tab.setRewriteTable(value);
                                }
                                this.tableSet(closure.env, key, value);
                                break;
                            }
                            this.tableSet(closure.env, key, value);
                            break;
                        }
                        case 8: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            uv = closure.upvalues[b];
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            uv.setValue(callFrame.get(a));
                            break;
                        }
                        case 9: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            aObj = callFrame.get(a);
                            key = this.getRegisterOrConstant(callFrame, b, prototype);
                            value = this.getRegisterOrConstant(callFrame, c, prototype);
                            this.tableSet(aObj, key, value);
                            break;
                        }
                        case 10: {
                            a = KahluaThread.getA8(op);
                            t = this.platform.newTable();
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, t);
                            break;
                        }
                        case 11: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            key = this.getRegisterOrConstant(callFrame, c, prototype);
                            bObj = callFrame.get(b);
                            KahluaThread.lastCallFrame = callFrame;
                            fun = this.tableget(bObj, key);
                            callFrame.set(a, fun);
                            callFrame.set(a + 1, bObj);
                            bStepWorthy = false;
                            break;
                        }
                        case 12: 
                        case 13: 
                        case 14: 
                        case 15: 
                        case 16: 
                        case 17: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            bo = this.getRegisterOrConstant(callFrame, b, prototype);
                            co = this.getRegisterOrConstant(callFrame, c, prototype);
                            bd = null;
                            cd = null;
                            res = null;
                            bd = KahluaUtil.rawTonumber(bo);
                            if (bd == null || (cd = KahluaUtil.rawTonumber(co)) == null) {
                                meta_op = KahluaThread.meta_ops[opcode];
                                metafun = this.getBinMetaOp(bo, co, meta_op);
                                if (metafun == null) {
                                    this.doStacktraceProper(callFrame);
                                    funcName = "unknown";
                                    if (closure.debugName != null) {
                                        funcName = closure.debugName;
                                    } else if (prototype.name != null) {
                                        funcName = prototype.name;
                                    }
                                    KahluaUtil.fail(meta_op + " not defined for operands in " + funcName);
                                }
                                res = this.call(metafun, bo, co, null);
                            } else {
                                res = this.primitiveMath(bd, cd, opcode);
                            }
                            if (Core.debug && this == LuaManager.thread) {
                                this.setLocalVarToStack(callFrame, a);
                            }
                            callFrame.set(a, res);
                            break;
                        }
                        case 18: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            aObj = callFrame.get(b);
                            aDouble = KahluaUtil.rawTonumber(aObj);
                            if (aDouble != null) {
                                res = KahluaUtil.toDouble(-KahluaUtil.fromDouble(aDouble));
                            } else {
                                metafun = this.getMetaOp(aObj, "__unm");
                                res = this.call(metafun, aObj, null, null);
                            }
                            callFrame.set(a, res);
                            break;
                        }
                        case 19: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            aObj = callFrame.get(b);
                            callFrame.set(a, KahluaUtil.toBoolean(KahluaUtil.boolEval(aObj) == false));
                            bStepWorthy = false;
                            break;
                        }
                        case 20: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            o = callFrame.get(b);
                            if (o instanceof KahluaTable) {
                                t = (KahluaTable)o;
                                res = KahluaUtil.toDouble(t.len());
                            } else if (o instanceof String) {
                                s = (String)o;
                                res = KahluaUtil.toDouble(s.length());
                            } else {
                                f = this.getMetaOp(o, "__len");
                                if (f == null) {
                                    this.doStacktraceProper(callFrame);
                                }
                                KahluaUtil.luaAssert(f != null, "__len not defined for operand");
                                res = this.call(f, o, null, null);
                            }
                            callFrame.set(a, res);
                            bStepWorthy = false;
                            break;
                        }
                        case 21: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            first = b;
                            last = c;
                            res = callFrame.get(last);
                            --last;
                            while (first <= last) {
                                resStr = KahluaUtil.rawTostring(res);
                                if (resStr != null) {
                                    nStrings = 0;
                                    pos = last;
                                    while (first <= pos) {
                                        o = callFrame.get(pos);
                                        --pos;
                                        if (KahluaUtil.rawTostring(o) == null) break;
                                        ++nStrings;
                                    }
                                    if (nStrings > 0) {
                                        concatBuffer = this.concatBuffer;
                                        concatBuffer.setLength(0);
                                        for (firstString = last - nStrings + 1; firstString <= last; ++firstString) {
                                            concatBuffer.append(KahluaUtil.rawTostring(callFrame.get(firstString)));
                                        }
                                        concatBuffer.append(resStr);
                                        res = concatBuffer.toString();
                                        last -= nStrings;
                                    }
                                }
                                if (first > last) continue;
                                leftConcat = callFrame.get(last);
                                metafun = this.getBinMetaOp(leftConcat, res, "__concat");
                                if (metafun == null) {
                                    KahluaUtil.fail("__concat not defined for operands: " + String.valueOf(leftConcat) + " and " + String.valueOf(res));
                                }
                                res = this.call(metafun, leftConcat, res, null);
                                --last;
                            }
                            callFrame.set(a, res);
                            bStepWorthy = false;
                            break;
                        }
                        case 22: {
                            callFrame.pc += KahluaThread.getSBx(op);
                            break;
                        }
                        case 23: 
                        case 24: 
                        case 25: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            bo = this.getRegisterOrConstant(callFrame, b, prototype);
                            co = this.getRegisterOrConstant(callFrame, c, prototype);
                            if (!(bo instanceof Double) || !(co instanceof Double)) ** GOTO lbl281
                            bd_primitive = KahluaUtil.fromDouble(bo);
                            cd_primitive = KahluaUtil.fromDouble(co);
                            if (opcode == 23) {
                                if (bd_primitive == cd_primitive == (a == 0)) {
                                    ++callFrame.pc;
                                }
                            } else if (opcode == 24) {
                                if (bd_primitive < cd_primitive == (a == 0)) {
                                    ++callFrame.pc;
                                }
                            } else if (bd_primitive <= cd_primitive == (a == 0)) {
                                ++callFrame.pc;
                            }
                            ** GOTO lbl321
lbl281:
                            // 1 sources

                            if (!(bo instanceof String)) ** GOTO lbl-1000
                            bs = (String)bo;
                            if (co instanceof String) {
                                cs = (String)co;
                                if (opcode == 23) {
                                    if (bo.equals(co) == (a == 0)) {
                                        ++callFrame.pc;
                                    }
                                } else {
                                    cmp = bs.compareTo(cs);
                                    if (opcode == 24) {
                                        if (cmp < 0 == (a == 0)) {
                                            ++callFrame.pc;
                                        }
                                    } else if (cmp <= 0 == (a == 0)) {
                                        ++callFrame.pc;
                                    }
                                }
                            } else lbl-1000:
                            // 2 sources

                            {
                                if (bo == co && opcode == 23) {
                                    resBool = true;
                                } else {
                                    invert = false;
                                    meta_op = KahluaThread.meta_ops[opcode];
                                    metafun = this.getCompMetaOp(bo, co, meta_op);
                                    if (metafun == null && opcode == 25) {
                                        metafun = this.getCompMetaOp(bo, co, "__lt");
                                        tmp = bo;
                                        bo = co;
                                        co = tmp;
                                        invert = true;
                                    }
                                    if (metafun == null && opcode == 23) {
                                        resBool = BaseLib.luaEquals(bo, co);
                                    } else {
                                        if (metafun == null) {
                                            this.doStacktraceProper(callFrame);
                                            KahluaUtil.fail(meta_op + " not defined for operand");
                                        }
                                        res = this.call(metafun, bo, co, null);
                                        resBool = KahluaUtil.boolEval(res);
                                    }
                                    if (invert) {
                                        resBool = resBool == false;
                                    }
                                }
                                if (resBool == (a == 0)) {
                                    ++callFrame.pc;
                                }
                            }
lbl321:
                            // 9 sources

                            bStepWorthy = false;
                            break;
                        }
                        case 26: {
                            a = KahluaThread.getA8(op);
                            c = KahluaThread.getC9(op);
                            value = callFrame.get(a);
                            if (KahluaUtil.boolEval(value) != (c == 0)) break;
                            ++callFrame.pc;
                            break;
                        }
                        case 27: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            value = callFrame.get(b);
                            if (KahluaUtil.boolEval(value) != (c == 0)) {
                                callFrame.set(a, value);
                                break;
                            }
                            ++callFrame.pc;
                            break;
                        }
                        case 28: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            nArguments2 = b - 1;
                            if (nArguments2 != -1) {
                                callFrame.setTop(a + nArguments2 + 1);
                            } else {
                                nArguments2 = callFrame.getTop() - a - 1;
                            }
                            callFrame.restoreTop = c != 0;
                            base = callFrame.localBase;
                            localBase2 = base + a + 1;
                            returnBase2 = base + a;
                            funObject = callFrame.get(a);
                            if (funObject == null) {
                                fdsfds = false;
                                funObject = callFrame.get(a);
                            }
                            if (funObject == null) {
                                this.doStacktraceProper(callFrame);
                                if (callFrame.getClosure().debugName != null) {
                                    KahluaUtil.fail("Object tried to call nil in " + callFrame.getClosure().debugName);
                                } else if (callFrame.getClosure().prototype != null && callFrame.getClosure().prototype.name != null) {
                                    KahluaUtil.fail("Object tried to call nil in " + callFrame.getClosure().prototype.name);
                                } else {
                                    KahluaUtil.fail("Object tried to call nil in unknown");
                                }
                            }
                            if ((fun = this.prepareMetatableCall(funObject)) == null) {
                                KahluaUtil.fail("Object " + String.valueOf(funObject) + " did not have __call metatable set");
                            }
                            if (fun != funObject) {
                                localBase2 = returnBase2;
                                ++nArguments2;
                            }
                            if (fun instanceof LuaClosure) {
                                luaClosure = (LuaClosure)fun;
                                newCallFrame = this.currentCoroutine.pushNewCallFrame(luaClosure, null, localBase2, returnBase2, nArguments2, true, callFrame.canYield);
                                newCallFrame.init();
                                callFrame = newCallFrame;
                                closure = newCallFrame.closure;
                                prototype = closure.prototype;
                                opcodes = prototype.code;
                                returnBase = callFrame.returnBase;
                                break;
                            }
                            if (fun instanceof JavaFunction) {
                                javaFunction = (JavaFunction)fun;
                                this.callJava(javaFunction, localBase2, returnBase2, nArguments2);
                                callFrame = this.currentCoroutine.currentCallFrame();
                                if (callFrame == null || callFrame.isJava()) {
                                    return;
                                }
                                closure = callFrame.closure;
                                prototype = closure.prototype;
                                opcodes = prototype.code;
                                returnBase = callFrame.returnBase;
                                if (!callFrame.restoreTop) break;
                                callFrame.setTop(prototype.maxStacksize);
                                break;
                            }
                            throw new RuntimeException("Tried to call a non-function: " + String.valueOf(fun));
                        }
                        case 29: {
                            base = callFrame.localBase;
                            this.currentCoroutine.closeUpvalues(base);
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            nArguments2 = b - 1;
                            if (nArguments2 == -1) {
                                nArguments2 = callFrame.getTop() - a - 1;
                            }
                            callFrame.restoreTop = false;
                            funObject = callFrame.get(a);
                            try {
                                KahluaUtil.luaAssert(funObject != null, "Tried to call nil");
                            }
                            catch (Exception ex) {
                                if (Core.debug && UIManager.defaultthread == LuaManager.thread) {
                                    UIManager.debugBreakpoint(LuaManager.thread.currentfile, LuaManager.thread.currentLine - 1);
                                }
                                this.debugException(ex);
                                this.doStacktraceProper(callFrame);
                                KahluaUtil.fail("");
                            }
                            fun = this.prepareMetatableCall(funObject);
                            if (fun == null) {
                                KahluaUtil.fail("Object did not have __call metatable set");
                            }
                            localBase2 = returnBase + 1;
                            if (fun != funObject) {
                                localBase2 = returnBase;
                                ++nArguments2;
                            }
                            this.currentCoroutine.stackCopy(base + a, returnBase, nArguments2 + 1);
                            this.currentCoroutine.setTop(returnBase + nArguments2 + 1);
                            if (fun instanceof LuaClosure) {
                                luaClosure = (LuaClosure)fun;
                                callFrame.localBase = localBase2;
                                callFrame.nArguments = nArguments2;
                                callFrame.closure = luaClosure;
                                callFrame.init();
                            } else {
                                if (!(fun instanceof JavaFunction)) {
                                    KahluaUtil.fail("Tried to call a non-function: " + String.valueOf(fun));
                                }
                                oldCoroutine = this.currentCoroutine;
                                this.callJava((JavaFunction)fun, localBase2, returnBase, nArguments2);
                                callFrame = this.currentCoroutine.currentCallFrame();
                                oldCoroutine.popCallFrame();
                                if (oldCoroutine != this.currentCoroutine) {
                                    if (oldCoroutine.isDead() && oldCoroutine != this.rootCoroutine && this.currentCoroutine.getParent() == oldCoroutine) {
                                        this.currentCoroutine.resume(oldCoroutine.getParent());
                                        oldCoroutine.destroy();
                                        this.currentCoroutine.getParent().currentCallFrame().push(Boolean.TRUE);
                                    }
                                    if ((callFrame = this.currentCoroutine.currentCallFrame()).isJava()) {
                                        return;
                                    }
                                } else {
                                    if (!callFrame.fromLua) {
                                        return;
                                    }
                                    callFrame = this.currentCoroutine.currentCallFrame();
                                    if (callFrame.restoreTop) {
                                        callFrame.setTop(callFrame.closure.prototype.maxStacksize);
                                    }
                                }
                            }
                            closure = callFrame.closure;
                            prototype = closure.prototype;
                            opcodes = prototype.code;
                            returnBase = callFrame.returnBase;
                            break;
                        }
                        case 30: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op) - 1;
                            base = callFrame.localBase;
                            this.currentCoroutine.closeUpvalues(base);
                            if (b == -1) {
                                b = callFrame.getTop() - a;
                            }
                            this.currentCoroutine.stackCopy(callFrame.localBase + a, returnBase, b);
                            this.currentCoroutine.setTop(returnBase + b);
                            if (callFrame.fromLua) {
                                if (callFrame.canYield && this.currentCoroutine.atBottom()) {
                                    callFrame.localBase = callFrame.returnBase;
                                    coroutine = this.currentCoroutine;
                                    Coroutine.yieldHelper(callFrame, callFrame, b);
                                    coroutine.popCallFrame();
                                    callFrame = this.currentCoroutine.currentCallFrame();
                                    if (callFrame == null || callFrame.isJava()) {
                                        return;
                                    }
                                } else {
                                    this.currentCoroutine.popCallFrame();
                                }
                                callFrame = this.currentCoroutine.currentCallFrame();
                                closure = callFrame.closure;
                                prototype = closure.prototype;
                                opcodes = prototype.code;
                                returnBase = callFrame.returnBase;
                                if (!callFrame.restoreTop) break;
                                callFrame.setTop(prototype.maxStacksize);
                                break;
                            }
                            this.currentCoroutine.popCallFrame();
                            return;
                        }
                        case 32: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getSBx(op);
                            iter = KahluaUtil.fromDouble(callFrame.get(a));
                            step = KahluaUtil.fromDouble(callFrame.get(a + 2));
                            callFrame.set(a, KahluaUtil.toDouble(iter - step));
                            callFrame.pc += b;
                            break;
                        }
                        case 31: {
                            a = KahluaThread.getA8(op);
                            iter = KahluaUtil.fromDouble(callFrame.get(a));
                            end = KahluaUtil.fromDouble(callFrame.get(a + 1));
                            step = KahluaUtil.fromDouble(callFrame.get(a + 2));
                            iterDouble = KahluaUtil.toDouble(iter += step);
                            callFrame.set(a, iterDouble);
                            if (step > 0.0 ? iter <= end : iter >= end) {
                                b = KahluaThread.getSBx(op);
                                callFrame.pc += b;
                                callFrame.set(a + 3, iterDouble);
                                break;
                            }
                            callFrame.clearFromIndex(a);
                            break;
                        }
                        case 33: {
                            a = KahluaThread.getA8(op);
                            c = KahluaThread.getC9(op);
                            callFrame.setTop(a + 6);
                            callFrame.stackCopy(a, a + 3, 3);
                            this.call(2);
                            callFrame.clearFromIndex(a + 3 + c);
                            callFrame.setPrototypeStacksize();
                            aObj3 = callFrame.get(a + 3);
                            if (aObj3 != null) {
                                callFrame.set(a + 2, aObj3);
                                break;
                            }
                            ++callFrame.pc;
                            break;
                        }
                        case 34: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op);
                            c = KahluaThread.getC9(op);
                            if (b == 0) {
                                b = callFrame.getTop() - a - 1;
                            }
                            if (c == 0) {
                                c = opcodes[callFrame.pc++];
                            }
                            offset = (c - 1) * 50;
                            t = (KahluaTable)callFrame.get(a);
                            for (i = 1; i <= b; ++i) {
                                key = KahluaUtil.toDouble(offset + i);
                                value = callFrame.get(a + i);
                                t.rawset(key, value);
                            }
                            break;
                        }
                        case 35: {
                            a = KahluaThread.getA8(op);
                            callFrame.closeUpvalues(a);
                            break;
                        }
                        case 36: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getBx(op);
                            newPrototype = prototype.prototypes[b];
                            newClosure = new LuaClosure(newPrototype, closure.env);
                            callFrame.set(a, newClosure);
                            numUpvalues = newPrototype.numUpvalues;
                            block46: for (i = 0; i < numUpvalues; ++i) {
                                op = opcodes[callFrame.pc++];
                                opcode = op & 63;
                                b = KahluaThread.getB9(op);
                                switch (opcode) {
                                    case 0: {
                                        newClosure.upvalues[i] = callFrame.findUpvalue(b);
                                        continue block46;
                                    }
                                    case 4: {
                                        newClosure.upvalues[i] = closure.upvalues[b];
                                        continue block46;
                                    }
                                }
                            }
                            break;
                        }
                        case 37: {
                            a = KahluaThread.getA8(op);
                            b = KahluaThread.getB9(op) - 1;
                            callFrame.pushVarargs(a, b);
                            break;
                        }
                    }
                }
                catch (Throwable e) {
                    ExceptionLogger.logException(e);
                    this.debugException(e);
                    this.doStacktraceProper(callFrame);
                    if (Core.debug && UIManager.defaultthread == LuaManager.thread) {
                        DebugType.Lua.printStackTrace(LogSeverity.Error, -1, "DEBUG-MODE STACK TRACE:", new Object[0]);
                        UIManager.debugBreakpoint(LuaManager.thread.currentfile, LuaManager.thread.currentLine - 1);
                    }
                    rethrow = true;
                    do {
                        if ((callFrame = this.currentCoroutine.currentCallFrame()) == null) {
                            parent = this.currentCoroutine.getParent();
                            if (parent == null) break;
                            nextCallFrame = parent.currentCallFrame();
                            nextCallFrame.push(Boolean.FALSE);
                            nextCallFrame.push(e.getMessage());
                            nextCallFrame.push(this.currentCoroutine.stackTrace);
                            this.currentCoroutine.destroy();
                            this.currentCoroutine = parent;
                            callFrame = this.currentCoroutine.currentCallFrame();
                            closure = callFrame.closure;
                            prototype = closure.prototype;
                            opcodes = prototype.code;
                            returnBase = callFrame.returnBase;
                            rethrow = false;
                            break;
                        }
                        this.currentCoroutine.addStackTrace(callFrame);
                        this.currentCoroutine.popCallFrame();
                    } while (callFrame.fromLua);
                    if (callFrame != null) {
                        callFrame.closeUpvalues(0);
                    }
                    if (!rethrow) break block148;
                    throw e;
                }
            }
            if (!this.reset) continue;
            throw new RuntimeException("lua was reset");
        }
    }

    private void DoProfileTiming(String file, long start, long end) {
    }

    public StringBuilder startErrorMessage() {
        this.stringBuilder.setLength(0);
        return this.stringBuilder;
    }

    public void flushErrorMessage() {
        String str = this.stringBuilder.toString().stripTrailing();
        DebugType.General.error(str);
        while (m_errors_list.size() >= 40) {
            m_errors_list.remove(0);
        }
        m_errors_list.add(str);
        ++errorCount;
    }

    public void doStacktraceProper(LuaCallFrame callFrame) {
        LuaCallFrame f;
        if (callFrame == null) {
            return;
        }
        StringBuilder sb = this.startErrorMessage();
        sb.append("dumping Lua stack trace\n");
        sb.append("-----------------------------------------\n");
        sb.append("STACK TRACE\n");
        sb.append("-----------------------------------------\n");
        sb.append(StackTraceContainer.getStackTraceString(LuaManager.getLuaStackStrace(callFrame.coroutine), "\t", 0, -1));
        this.flushErrorMessage();
        if (Core.debug && !GameServer.server && (f = callFrame.coroutine.getCallFrame(callFrame.coroutine.getCallframeTop() - 1)) != null) {
            LuaManager.debugcaller.pcall(LuaManager.debugthread, LuaManager.env.rawget("DoLuaError"), f.getFilename(), f.pc);
        }
    }

    public void doStacktraceProper() {
        LuaCallFrame callFrame = this.currentCoroutine.currentCallFrame();
        this.doStacktraceProper(callFrame);
    }

    public void debugException(Throwable ex) {
        this.stringWriter.getBuffer().setLength(0);
        ex.printStackTrace(this.printWriter);
        String str = this.stringWriter.toString();
        m_errors_list.add(str);
        ++errorCount;
    }

    protected Object getMetaOp(Object o, String meta_op) {
        KahluaTable meta = (KahluaTable)this.getmetatable(o, true);
        if (meta == null) {
            return null;
        }
        return meta.rawget(meta_op);
    }

    private final Object getCompMetaOp(Object a, Object b, String meta_op) {
        Object meta_operator2;
        KahluaTable meta1 = (KahluaTable)this.getmetatable(a, true);
        KahluaTable meta2 = (KahluaTable)this.getmetatable(b, true);
        if (meta1 == null || meta2 == null) {
            return null;
        }
        Object meta_operator1 = meta1.rawget(meta_op);
        if (meta_operator1 != (meta_operator2 = meta2.rawget(meta_op)) || meta_operator1 == null) {
            return null;
        }
        return meta_operator1;
    }

    private final Object getBinMetaOp(Object a, Object b, String meta_op) {
        Object op = this.getMetaOp(a, meta_op);
        if (op != null) {
            return op;
        }
        return this.getMetaOp(b, meta_op);
    }

    private final Object getRegisterOrConstant(LuaCallFrame callFrame, int index, Prototype prototype) {
        int cindex = index - 256;
        if (cindex < 0) {
            return callFrame.get(index);
        }
        return prototype.constants[cindex];
    }

    private static final int getA8(int op) {
        return op >>> 6 & 0xFF;
    }

    private static final int getC9(int op) {
        return op >>> 14 & 0x1FF;
    }

    private static final int getB9(int op) {
        return op >>> 23 & 0x1FF;
    }

    private static final int getBx(int op) {
        return op >>> 14;
    }

    private static final int getSBx(int op) {
        return (op >>> 14) - 131071;
    }

    private Double primitiveMath(Double x, Double y, int opcode) {
        double v1 = KahluaUtil.fromDouble(x);
        double v2 = KahluaUtil.fromDouble(y);
        double res = 0.0;
        switch (opcode) {
            case 12: {
                res = v1 + v2;
                break;
            }
            case 13: {
                res = v1 - v2;
                break;
            }
            case 14: {
                res = v1 * v2;
                break;
            }
            case 15: {
                res = v1 / v2;
                break;
            }
            case 16: {
                if (v2 == 0.0) {
                    res = Double.NaN;
                    break;
                }
                int ipart = (int)(v1 / v2);
                res = v1 - (double)ipart * v2;
                break;
            }
            case 17: {
                res = this.platform.pow(v1, v2);
                break;
            }
        }
        return KahluaUtil.toDouble(res);
    }

    public Object call(Object fun, Object arg1, Object arg2, Object arg3) {
        int oldTop = this.currentCoroutine.getTop();
        int argslen = 3;
        this.currentCoroutine.setTop(oldTop + 1 + 3);
        this.currentCoroutine.objectStack[oldTop] = fun;
        this.currentCoroutine.objectStack[oldTop + 1] = arg1;
        this.currentCoroutine.objectStack[oldTop + 2] = arg2;
        this.currentCoroutine.objectStack[oldTop + 3] = arg3;
        int nReturnValues = this.call(3);
        Object ret = null;
        if (nReturnValues >= 1) {
            ret = this.currentCoroutine.objectStack[oldTop];
        }
        this.currentCoroutine.setTop(oldTop);
        return ret;
    }

    public Object call(Object fun, Object[] args2) {
        int oldTop = this.currentCoroutine.getTop();
        int argslen = args2 == null ? 0 : args2.length;
        this.currentCoroutine.setTop(oldTop + 1 + argslen);
        this.currentCoroutine.objectStack[oldTop] = fun;
        for (int i = 1; i <= argslen; ++i) {
            this.currentCoroutine.objectStack[oldTop + i] = args2[i - 1];
        }
        int nReturnValues = this.call(argslen);
        Object ret = null;
        if (nReturnValues >= 1) {
            ret = this.currentCoroutine.objectStack[oldTop];
        }
        this.currentCoroutine.setTop(oldTop);
        return ret;
    }

    public Object tableget(Object table, Object key) {
        Object curObj = table;
        for (int i = 100; i > 0; --i) {
            KahluaTable t;
            Object res;
            boolean isTable = curObj instanceof KahluaTable;
            if (isTable && (res = (t = (KahluaTable)curObj).rawget(key)) != null) {
                return res;
            }
            Object metaOp = this.getMetaOp(curObj, "__index");
            if (metaOp == null) {
                if (isTable) {
                    return null;
                }
                StringBuilder sb = this.startErrorMessage();
                sb.append("-------------------------------------------------------------\n");
                sb.append("attempted index: " + String.valueOf(key) + " of non-table: " + String.valueOf(curObj) + "\n");
                this.flushErrorMessage();
                this.doStacktraceProper(this.currentCoroutine.currentCallFrame());
                throw new RuntimeException("attempted index: " + String.valueOf(key) + " of non-table: " + String.valueOf(curObj));
            }
            if (metaOp instanceof JavaFunction || metaOp instanceof LuaClosure) {
                res = this.call(metaOp, table, key, null);
                return res;
            }
            curObj = metaOp;
        }
        throw new RuntimeException("loop in gettable");
    }

    public void tableSet(Object table, Object key, Object value) {
        Object curObj = table;
        for (int i = 100; i > 0; --i) {
            Object metaOp;
            if (curObj instanceof KahluaTable) {
                KahluaTable t = (KahluaTable)curObj;
                if (t.rawget(key) != null) {
                    t.rawset(key, value);
                    return;
                }
                metaOp = this.getMetaOp(curObj, "__newindex");
                if (metaOp == null) {
                    t.rawset(key, value);
                    return;
                }
            } else {
                metaOp = this.getMetaOp(curObj, "__newindex");
                if (metaOp == null) {
                    this.doStacktraceProper(this.currentCoroutine.currentCallFrame());
                }
                KahluaUtil.luaAssert(metaOp != null, "attempted index of non-table");
            }
            if (metaOp instanceof JavaFunction || metaOp instanceof LuaClosure) {
                this.call(metaOp, table, key, value);
                return;
            }
            curObj = metaOp;
        }
        throw new RuntimeException("loop in settable");
    }

    public void setmetatable(Object o, KahluaTable metatable) {
        KahluaUtil.luaAssert(o != null, "Can't set metatable for nil");
        if (o instanceof KahluaTable) {
            KahluaTable t = (KahluaTable)o;
            t.setMetatable(metatable);
        } else {
            KahluaUtil.fail("Could not set metatable for object");
        }
    }

    public Object getmetatable(Object o, boolean raw) {
        Object meta2;
        KahluaTable metatable;
        if (o == null) {
            return null;
        }
        if (o instanceof KahluaTable) {
            KahluaTable kahluaTable = (KahluaTable)o;
            metatable = kahluaTable.getMetatable();
        } else {
            metatable = this.getClassMetatable(o.getClass());
        }
        if (!raw && metatable != null && (meta2 = metatable.rawget("__metatable")) != null) {
            return meta2;
        }
        return metatable;
    }

    private KahluaTable getClassMetatable(Class<?> c) {
        Class clazz;
        if (this.cachedMetatables.containsKey(c)) {
            KahluaTable possiblyNull = this.cachedMetatables.get(c);
            return possiblyNull;
        }
        KahluaTable metatables = KahluaUtil.getClassMetatables(this.platform, this.getEnvironment());
        ArrayDeque queue = new ArrayDeque();
        queue.add(c);
        KahluaTable metatable = null;
        while (!queue.isEmpty() && (metatable = (KahluaTable)this.tableget(metatables, clazz = (Class)queue.poll())) == null) {
            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                queue.add(superclass);
            }
            Collections.addAll(queue, clazz.getInterfaces());
        }
        this.cachedMetatables.put(c, metatable);
        return metatable;
    }

    public Object[] pcall(Object fun, Object[] args2) {
        int nArgs = args2 == null ? 0 : args2.length;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + nArgs);
        coroutine.objectStack[oldTop] = fun;
        if (nArgs > 0) {
            System.arraycopy(args2, 0, coroutine.objectStack, oldTop + 1, nArgs);
        }
        int nRet = this.pcall(nArgs);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        Object[] ret = null;
        ret = args2.length == nRet ? args2 : new Object[nRet];
        System.arraycopy(coroutine.objectStack, oldTop, ret, 0, nRet);
        coroutine.setTop(oldTop);
        return ret;
    }

    public void pcallvoid(Object fun, Object[] args2) {
        int nArgs = args2 == null ? 0 : args2.length;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + nArgs);
        coroutine.objectStack[oldTop] = fun;
        if (nArgs > 0) {
            System.arraycopy(args2, 0, coroutine.objectStack, oldTop + 1, nArgs);
        }
        int nRet = this.pcall(nArgs);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        coroutine.setTop(oldTop);
    }

    public void pcallvoid(Object fun, Object arg) {
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 1);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        int nRet = this.pcall(1);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        coroutine.setTop(oldTop);
    }

    public void pcallvoid(Object fun, Object arg, Object arg2) {
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 2);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        coroutine.objectStack[oldTop + 2] = arg2;
        int nRet = this.pcall(2);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        coroutine.setTop(oldTop);
    }

    public void pcallvoid(Object fun, Object arg, Object arg2, Object arg3) {
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 3);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        coroutine.objectStack[oldTop + 2] = arg2;
        coroutine.objectStack[oldTop + 3] = arg3;
        int nRet = this.pcall(3);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        coroutine.setTop(oldTop);
    }

    public Boolean pcallBoolean(Object fun, Object arg) {
        Object functionResult;
        Boolean calledOK;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 1);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        int nRet = this.pcall(1);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        Boolean ret = null;
        if (nRet > 1 && (calledOK = (Boolean)coroutine.objectStack[oldTop]).booleanValue() && (functionResult = coroutine.objectStack[oldTop + 1]) instanceof Boolean) {
            Boolean b = (Boolean)functionResult;
            ret = b != false ? Boolean.TRUE : Boolean.FALSE;
        }
        coroutine.setTop(oldTop);
        return ret;
    }

    public Boolean pcallBoolean(Object fun, Object arg, Object arg2) {
        Object functionResult;
        Boolean calledOK;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 2);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        coroutine.objectStack[oldTop + 2] = arg2;
        int nRet = this.pcall(2);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        Boolean ret = null;
        if (nRet > 1 && (calledOK = (Boolean)coroutine.objectStack[oldTop]).booleanValue() && (functionResult = coroutine.objectStack[oldTop + 1]) instanceof Boolean) {
            Boolean b = (Boolean)functionResult;
            ret = b != false ? Boolean.TRUE : Boolean.FALSE;
        }
        coroutine.setTop(oldTop);
        return ret;
    }

    public Boolean pcallBoolean(Object fun, Object arg, Object arg2, Object arg3) {
        Object functionResult;
        Boolean calledOK;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + 3);
        coroutine.objectStack[oldTop] = fun;
        coroutine.objectStack[oldTop + 1] = arg;
        coroutine.objectStack[oldTop + 2] = arg2;
        coroutine.objectStack[oldTop + 3] = arg3;
        int nRet = this.pcall(3);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        Boolean ret = null;
        if (nRet > 1 && (calledOK = (Boolean)coroutine.objectStack[oldTop]).booleanValue() && (functionResult = coroutine.objectStack[oldTop + 1]) instanceof Boolean) {
            Boolean b = (Boolean)functionResult;
            ret = b != false ? Boolean.TRUE : Boolean.FALSE;
        }
        coroutine.setTop(oldTop);
        return ret;
    }

    public Boolean pcallBoolean(Object fun, Object[] args2) {
        Object functionResult;
        Boolean calledOK;
        int nArgs = args2 == null ? 0 : args2.length;
        Coroutine coroutine = this.currentCoroutine;
        int oldTop = coroutine.getTop();
        coroutine.setTop(oldTop + 1 + nArgs);
        coroutine.objectStack[oldTop] = fun;
        if (nArgs > 0) {
            System.arraycopy(args2, 0, coroutine.objectStack, oldTop + 1, nArgs);
        }
        int nRet = this.pcall(nArgs);
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        Boolean ret = null;
        if (nRet > 1 && (calledOK = (Boolean)coroutine.objectStack[oldTop]).booleanValue() && (functionResult = coroutine.objectStack[oldTop + 1]) instanceof Boolean) {
            Boolean b = (Boolean)functionResult;
            ret = b != false ? Boolean.TRUE : Boolean.FALSE;
        }
        coroutine.setTop(oldTop);
        return ret;
    }

    public Object[] pcall(Object fun) {
        return this.pcall(fun, null);
    }

    public int pcall(int nArguments) {
        Object errorMessage;
        Throwable exception;
        if (this.debugOwnerThread != Thread.currentThread()) {
            DebugType.Lua.error("Lua code called from the wrong thread, owner=%s current=%s", this.debugOwnerThread.getName(), Thread.currentThread().getName());
            DebugType.Lua.printStackTrace();
        }
        Coroutine coroutine = this.currentCoroutine;
        LuaCallFrame currentCallFrame = coroutine.currentCallFrame();
        coroutine.stackTrace = "";
        int oldBase = coroutine.getTop() - nArguments - 1;
        try {
            boolean bl;
            int oldCallframetop = coroutine.getCallframeTop();
            int nValues = this.call(nArguments);
            int newCallframeTop = coroutine.getCallframeTop();
            if (oldCallframetop != newCallframeTop) {
                bl = false;
            }
            KahluaUtil.luaAssert(oldCallframetop == newCallframeTop, "error - call stack depth changed.");
            if (oldCallframetop != newCallframeTop) {
                bl = false;
            }
            int newTop = oldBase + nValues + 1;
            coroutine.setTop(newTop);
            coroutine.stackCopyNoDebugStuff(oldBase, oldBase + 1, nValues);
            coroutine.objectStack[oldBase] = Boolean.TRUE;
            return 1 + nValues;
        }
        catch (KahluaException e) {
            exception = e;
            errorMessage = e.errorMessage;
        }
        catch (Throwable e) {
            exception = e;
            errorMessage = e.getMessage() + " " + e.getClass().getName();
        }
        KahluaUtil.luaAssert(coroutine == this.currentCoroutine, "Internal Kahlua error - coroutine changed in pcall");
        if (currentCallFrame != null) {
            currentCallFrame.closeUpvalues(0);
        }
        coroutine.cleanCallFrames(currentCallFrame);
        coroutine.setTop(oldBase + 4);
        coroutine.objectStack[oldBase] = Boolean.FALSE;
        coroutine.objectStack[oldBase + 1] = errorMessage;
        coroutine.objectStack[oldBase + 2] = coroutine.stackTrace;
        coroutine.objectStack[oldBase + 3] = exception;
        coroutine.stackTrace = "";
        return 4;
    }

    public KahluaTable getEnvironment() {
        return this.currentCoroutine.environment;
    }

    public PrintStream getOut() {
        return this.out;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public void breakpointToggle(String file, int line) {
        ArrayList<Object> i;
        if (!this.breakpointMap.containsKey(file)) {
            i = new ArrayList();
            this.breakpointMap.put(file, i);
        } else {
            i = this.breakpointMap.get(file);
        }
        if (!i.contains(line)) {
            i.add(line);
        } else {
            i.remove((Object)line);
        }
    }

    public boolean hasBreakpoint(String file, int line) {
        return this.breakpointMap.containsKey(file) && this.breakpointMap.get(file).contains(line);
    }

    public void toggleBreakOnChange(KahluaTable table, Object key) {
        ArrayList<Object> i;
        if (!this.breakpointDataMap.containsKey(table)) {
            i = new ArrayList();
            this.breakpointDataMap.put(table, i);
        } else {
            i = this.breakpointDataMap.get(table);
        }
        if (!i.contains(key)) {
            i.add(key);
        } else {
            i.remove(key);
        }
    }

    public void toggleBreakOnRead(KahluaTable table, Object key) {
        ArrayList<Object> i;
        if (!this.breakpointReadDataMap.containsKey(table)) {
            i = new ArrayList();
            this.breakpointReadDataMap.put(table, i);
        } else {
            i = this.breakpointReadDataMap.get(table);
        }
        if (!i.contains(key)) {
            i.add(key);
        } else {
            i.remove(key);
        }
    }

    public boolean hasDataBreakpoint(KahluaTable kahluaTableImpl, Object key) {
        if (!this.breakpointDataMap.containsKey(kahluaTableImpl)) {
            return false;
        }
        ArrayList<Object> i = this.breakpointDataMap.get(kahluaTableImpl);
        return i.contains(key);
    }

    public boolean hasReadDataBreakpoint(KahluaTable kahluaTableImpl, Object key) {
        if (!this.breakpointReadDataMap.containsKey(kahluaTableImpl)) {
            return false;
        }
        ArrayList<Object> i = this.breakpointReadDataMap.get(kahluaTableImpl);
        return i.contains(key);
    }

    protected void setLocalVarToStack(LuaCallFrame callFrame, int a) {
        boolean bLastOnLine;
        Prototype prototype1 = callFrame.closure.prototype;
        int[] locvarlines = prototype1.locvarlines;
        if (locvarlines == null) {
            return;
        }
        int line = prototype1.lines[callFrame.pc - 1];
        boolean bl = bLastOnLine = prototype1.lines[callFrame.pc] != line;
        if (!bLastOnLine) {
            return;
        }
        while (callFrame.localsAssigned < locvarlines.length && line > locvarlines[callFrame.localsAssigned] && locvarlines[callFrame.localsAssigned] != 0) {
            ++callFrame.localsAssigned;
        }
        if (callFrame.localsAssigned < locvarlines.length && locvarlines[callFrame.localsAssigned] == line) {
            int n = callFrame.localsAssigned++;
            String name = prototype1.locvars[n];
            callFrame.setLocalVarToStack(name, callFrame.localBase + a);
        }
    }

    static {
        KahluaThread.meta_ops[12] = "__add";
        KahluaThread.meta_ops[13] = "__sub";
        KahluaThread.meta_ops[14] = "__mul";
        KahluaThread.meta_ops[15] = "__div";
        KahluaThread.meta_ops[16] = "__mod";
        KahluaThread.meta_ops[17] = "__pow";
        KahluaThread.meta_ops[23] = "__eq";
        KahluaThread.meta_ops[24] = "__lt";
        KahluaThread.meta_ops[25] = "__le";
        CLASS_NAME = KahluaThread.class.getName();
        m_errors_list = new ArrayList();
    }

    private static class ProfileEntryComparitor
    implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            double dist1 = o1.time;
            double dist2 = o2.time;
            if (dist1 > dist2) {
                return -1;
            }
            if (dist2 > dist1) {
                return 1;
            }
            return 0;
        }
    }

    public static class Entry {
        public String file;
        public double time;
    }
}

