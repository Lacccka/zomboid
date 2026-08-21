/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.util.ArrayList;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import se.krka.kahlua.vm.Prototype;
import se.krka.kahlua.vm.UpValue;
import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.utils.HashMap;

public final class LuaCallFrame {
    private final Platform platform;
    public final Coroutine coroutine;
    public LuaClosure closure;
    public JavaFunction javaFunction;
    public int pc;
    public int localBase;
    int returnBase;
    public int nArguments;
    boolean fromLua;
    public boolean canYield;
    boolean restoreTop;
    public int localsAssigned;
    public final HashMap localVarToStackMap = new HashMap();
    public final HashMap localStackToVarMap = new HashMap();
    public final ArrayList<String> localVarNames = new ArrayList();

    public LuaCallFrame(Coroutine coroutine) {
        this.coroutine = coroutine;
        this.platform = coroutine.getPlatform();
    }

    public String getFilename() {
        if (this.closure != null) {
            return this.closure.prototype.filename;
        }
        return null;
    }

    public final void set(int index, Object o) {
        this.coroutine.objectStack[this.localBase + index] = o;
    }

    public final Object get(int index) {
        return this.coroutine.objectStack[this.localBase + index];
    }

    public int push(Object x) {
        int top = this.getTop();
        this.setTop(top + 1);
        this.set(top, x);
        return 1;
    }

    public int push(Object x, Object y) {
        int top = this.getTop();
        this.setTop(top + 2);
        this.set(top, x);
        this.set(top + 1, y);
        return 2;
    }

    public int pushNil() {
        return this.push(null);
    }

    public final void stackCopy(int startIndex, int destIndex, int len) {
        this.coroutine.stackCopy(this.localBase + startIndex, this.localBase + destIndex, len);
    }

    public void stackClear(int startIndex, int endIndex) {
        while (startIndex <= endIndex) {
            this.coroutine.objectStack[this.localBase + startIndex] = null;
            ++startIndex;
        }
    }

    public void clearFromIndex(int index) {
        if (this.getTop() < index) {
            this.setTop(index);
        }
        this.stackClear(index, this.getTop() - 1);
    }

    public final void setTop(int index) {
        this.coroutine.setTop(this.localBase + index);
    }

    public void closeUpvalues(int a) {
        this.coroutine.closeUpvalues(this.localBase + a);
    }

    public UpValue findUpvalue(int b) {
        return this.coroutine.findUpvalue(this.localBase + b);
    }

    public int getTop() {
        return this.coroutine.getTop() - this.localBase;
    }

    public void init() {
        if (this.isLua()) {
            this.pc = 0;
            if (this.closure.prototype.isVararg) {
                this.localBase += this.nArguments;
                this.setTop(this.closure.prototype.maxStacksize);
                int toCopy = Math.min(this.nArguments, this.closure.prototype.numParams);
                this.stackCopy(-this.nArguments, 0, toCopy);
            } else {
                this.setTop(this.closure.prototype.maxStacksize);
                this.stackClear(this.closure.prototype.numParams, this.nArguments);
            }
        }
    }

    public void setPrototypeStacksize() {
        if (this.isLua()) {
            this.setTop(this.closure.prototype.maxStacksize);
        }
    }

    public void pushVarargs(int index, int n) {
        int nParams = this.closure.prototype.numParams;
        int nVarargs = this.nArguments - nParams;
        if (nVarargs < 0) {
            nVarargs = 0;
        }
        if (n == -1) {
            n = nVarargs;
            this.setTop(index + n);
        }
        if (nVarargs > n) {
            nVarargs = n;
        }
        this.stackCopy(-this.nArguments + nParams, index, nVarargs);
        int numNils = n - nVarargs;
        if (numNils > 0) {
            this.stackClear(index + nVarargs, index + n - 1);
        }
    }

    public KahluaTable getEnvironment() {
        if (this.isLua()) {
            return this.closure.env;
        }
        return this.coroutine.environment;
    }

    public boolean isJava() {
        return !this.isLua();
    }

    public boolean isLua() {
        return this.closure != null;
    }

    public String toString2() {
        if (this.closure != null) {
            return this.closure.toString2(this.pc);
        }
        if (this.javaFunction != null) {
            return "Callframe at: " + this.javaFunction.toString();
        }
        return super.toString();
    }

    public StackTraceElement toStackTraceElement() {
        return this.closure != null ? this.closure.toStackTraceElement(this.pc) : null;
    }

    public String toString() {
        if (this.closure != null) {
            return "Callframe at: " + this.closure.toString();
        }
        if (this.javaFunction != null) {
            return "Callframe at: " + this.javaFunction.toString();
        }
        return super.toString();
    }

    public Platform getPlatform() {
        return this.platform;
    }

    void setup(LuaClosure closure, JavaFunction javaFunction, int localBase, int returnBase, int nArguments, boolean fromLua, boolean insideCoroutine) {
        this.localBase = localBase;
        this.returnBase = returnBase;
        this.nArguments = nArguments;
        this.fromLua = fromLua;
        this.canYield = insideCoroutine;
        this.closure = closure;
        this.javaFunction = javaFunction;
        if (Core.debug) {
            LuaCallFrame f = this;
            f.localsAssigned = 0;
            if (!f.localVarToStackMap.isEmpty()) {
                f.localVarToStackMap.clear();
                f.localStackToVarMap.clear();
                f.localVarNames.clear();
            }
            if (f.closure != null && f.closure.prototype.locvarlines != null && this.getThread() == LuaManager.thread) {
                Prototype prototype = f.closure.prototype;
                int line = prototype.lines[0];
                int[] locvarlines = prototype.locvarlines;
                for (int n = localBase; n < localBase + nArguments; ++n) {
                    int m;
                    if (f.localsAssigned >= locvarlines.length || locvarlines[f.localsAssigned] >= line || locvarlines[f.localsAssigned] == 0) continue;
                    ++f.localsAssigned;
                    String name = prototype.locvars[m];
                    f.setLocalVarToStack(name, n);
                }
            }
        }
    }

    public KahluaThread getThread() {
        return this.coroutine.getThread();
    }

    public LuaClosure getClosure() {
        return this.closure;
    }

    public void setLocalVarToStack(String name, int a) {
        this.localVarToStackMap.put(name, a);
        this.localStackToVarMap.put(a, name);
        this.localVarNames.add(name);
    }

    public String getNameOfStack(int i) {
        if (this.localStackToVarMap.get(i) instanceof String) {
            return (String)this.localStackToVarMap.get(i);
        }
        return "";
    }

    public void printoutLocalVars() {
    }

    public int getLocalVarCount() {
        return this.localVarNames.size();
    }

    public String getLocalVarName(int index) {
        if (index >= 0 && index < this.localVarNames.size()) {
            return this.localVarNames.get(index);
        }
        return null;
    }

    public int getLocalVarStackIndex(int index) {
        String varName = this.getLocalVarName(index);
        if (varName == null) {
            return -1;
        }
        Object o = this.localVarToStackMap.get(varName);
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            return i;
        }
        return -1;
    }
}

