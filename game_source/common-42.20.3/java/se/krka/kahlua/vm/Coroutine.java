/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.util.ArrayList;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import se.krka.kahlua.vm.Prototype;
import se.krka.kahlua.vm.UpValue;
import zombie.Lua.LuaManager;
import zombie.UsedFromLua;
import zombie.core.Core;

@UsedFromLua
public final class Coroutine {
    private final Platform platform;
    private KahluaThread thread;
    private Coroutine parent;
    public KahluaTable environment;
    public String stackTrace = "";
    private final ArrayList<UpValue> liveUpvalues = new ArrayList();
    private static final int MAX_STACK_SIZE = 3000;
    private static final int INITIAL_STACK_SIZE = 1000;
    private static final int MAX_CALL_FRAME_STACK_SIZE = 1000;
    private static final int INITIAL_CALL_FRAME_STACK_SIZE = 200;
    public Object[] objectStack;
    private int top;
    private LuaCallFrame[] callFrameStack;
    private int callFrameTop;

    public Coroutine() {
        this.platform = null;
    }

    public Coroutine getParent() {
        return this.parent;
    }

    public Coroutine(Platform platform, KahluaTable environment, KahluaThread thread2) {
        this.platform = platform;
        this.environment = environment;
        this.thread = thread2;
        this.objectStack = new Object[1000];
        this.callFrameStack = new LuaCallFrame[200];
    }

    public Coroutine(Platform platform, KahluaTable environment) {
        this(platform, environment, null);
    }

    public LuaCallFrame pushNewCallFrame(LuaClosure closure, JavaFunction javaFunction, int localBase, int returnBase, int nArguments, boolean fromLua, boolean insideCoroutine) {
        this.setCallFrameStackTop(this.callFrameTop + 1);
        LuaCallFrame callFrame = this.currentCallFrame();
        callFrame.setup(closure, javaFunction, localBase, returnBase, nArguments, fromLua, insideCoroutine);
        return callFrame;
    }

    public void popCallFrame() {
        if (this.isDead()) {
            throw new RuntimeException("Stack underflow");
        }
        this.setCallFrameStackTop(this.callFrameTop - 1);
    }

    private void ensureCallFrameStackSize(int index) {
        int oldSize;
        int newSize;
        if (index > 1000) {
            throw new RuntimeException("Stack overflow");
        }
        for (newSize = oldSize = this.callFrameStack.length; newSize <= index; newSize = 2 * newSize) {
        }
        if (newSize > oldSize) {
            LuaCallFrame[] newStack = new LuaCallFrame[newSize];
            System.arraycopy(this.callFrameStack, 0, newStack, 0, oldSize);
            this.callFrameStack = newStack;
        }
    }

    public void setCallFrameStackTop(int newTop) {
        if (newTop > this.callFrameTop) {
            this.ensureCallFrameStackSize(newTop);
        } else {
            this.callFrameStackClear(newTop, this.callFrameTop - 1);
        }
        this.callFrameTop = newTop;
    }

    private void callFrameStackClear(int startIndex, int endIndex) {
        while (startIndex <= endIndex) {
            LuaCallFrame callFrame = this.callFrameStack[startIndex];
            if (callFrame != null) {
                this.callFrameStack[startIndex].closure = null;
                this.callFrameStack[startIndex].javaFunction = null;
            }
            ++startIndex;
        }
    }

    private void ensureStacksize(int index) {
        int oldSize;
        int newSize;
        if (index > 3000) {
            throw new RuntimeException("Stack overflow");
        }
        for (newSize = oldSize = this.objectStack.length; newSize <= index; newSize = 2 * newSize) {
        }
        if (newSize > oldSize) {
            Object[] newStack = new Object[newSize];
            System.arraycopy(this.objectStack, 0, newStack, 0, oldSize);
            this.objectStack = newStack;
        }
    }

    public void setTop(int newTop) {
        if (this.top < newTop) {
            this.ensureStacksize(newTop);
        } else {
            this.stackClear(newTop, this.top - 1);
        }
        this.top = newTop;
    }

    public void stackCopy(int startIndex, int destIndex, int len) {
        if (len > 0 && startIndex != destIndex) {
            System.arraycopy(this.objectStack, startIndex, this.objectStack, destIndex, len);
            LuaCallFrame f = this.getParentNoAssert(1);
            if (Core.debug && this.thread == LuaManager.thread && f != null && f.pc > 0 && f.closure != null && f.closure.prototype.locvarlines != null) {
                Prototype prototype = f.closure.prototype;
                int[] locvarlines = prototype.locvarlines;
                for (int n = destIndex; n < destIndex + len; ++n) {
                    int m;
                    boolean bLastOnLine;
                    int line = prototype.lines[f.pc - 1];
                    boolean bl = bLastOnLine = prototype.lines[f.pc] != line;
                    if (!bLastOnLine) continue;
                    while (f.localsAssigned < locvarlines.length && line > locvarlines[f.localsAssigned] && locvarlines[f.localsAssigned] != 0) {
                        ++f.localsAssigned;
                    }
                    if (f.localsAssigned >= locvarlines.length || locvarlines[f.localsAssigned] != line) continue;
                    ++f.localsAssigned;
                    String name = prototype.locvars[m];
                    f.setLocalVarToStack(name, n);
                }
            }
        }
    }

    public void stackCopyNoDebugStuff(int startIndex, int destIndex, int len) {
        if (len > 0 && startIndex != destIndex) {
            System.arraycopy(this.objectStack, startIndex, this.objectStack, destIndex, len);
        }
    }

    public void stackClear(int startIndex, int endIndex) {
        while (startIndex <= endIndex) {
            this.objectStack[startIndex] = null;
            ++startIndex;
        }
    }

    public void closeUpvalues(int closeIndex) {
        int loopIndex = this.liveUpvalues.size();
        while (--loopIndex >= 0) {
            UpValue uv = this.liveUpvalues.get(loopIndex);
            if (uv.getIndex() < closeIndex) {
                return;
            }
            uv.close();
            this.liveUpvalues.remove(loopIndex);
        }
    }

    public UpValue findUpvalue(int scanIndex) {
        UpValue uv;
        int loopIndex = this.liveUpvalues.size();
        while (--loopIndex >= 0) {
            uv = this.liveUpvalues.get(loopIndex);
            int index = uv.getIndex();
            if (index == scanIndex) {
                return uv;
            }
            if (index >= scanIndex) continue;
            break;
        }
        uv = new UpValue(this, scanIndex);
        this.liveUpvalues.add(loopIndex + 1, uv);
        return uv;
    }

    public Object getObjectFromStack(int n) {
        return this.objectStack[n];
    }

    public int getObjectStackSize() {
        return this.top;
    }

    public LuaCallFrame getParentCallframe() {
        int index = this.callFrameTop - 1;
        if (index < 0) {
            return null;
        }
        return this.callFrameStack[index];
    }

    public LuaCallFrame currentCallFrame() {
        if (this.isDead()) {
            return null;
        }
        LuaCallFrame callFrame = this.callFrameStack[this.callFrameTop - 1];
        if (callFrame == null) {
            this.callFrameStack[this.callFrameTop - 1] = callFrame = new LuaCallFrame(this);
        }
        return callFrame;
    }

    public int getTop() {
        return this.top;
    }

    public LuaCallFrame getParent(int level) {
        KahluaUtil.luaAssert(level >= 0, "Level must be non-negative");
        int index = this.callFrameTop - level - 1;
        KahluaUtil.luaAssert(index >= 0, "Level too high");
        return this.callFrameStack[index];
    }

    public LuaCallFrame getParentNoAssert(int level) {
        int index = this.callFrameTop - level - 1;
        if (index < 0) {
            return null;
        }
        return this.callFrameStack[index];
    }

    public String getCurrentStackTrace(int level, int count, int haltAt) {
        if (level < 0) {
            level = 0;
        }
        if (count < 0) {
            count = 0;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = this.callFrameTop - 1 - level; i >= haltAt && count-- > 0; --i) {
            buffer.append(this.getStackTrace(this.callFrameStack[i]));
        }
        return buffer.toString();
    }

    public void cleanCallFrames(LuaCallFrame callerFrame) {
        LuaCallFrame frame;
        while ((frame = this.currentCallFrame()) != null && frame != callerFrame) {
            this.addStackTrace(frame);
            this.popCallFrame();
        }
    }

    public void addStackTrace(LuaCallFrame frame) {
        this.stackTrace = this.stackTrace + this.getStackTrace(frame);
    }

    private String getStackTrace(LuaCallFrame frame) {
        if (frame.isLua()) {
            int pc;
            int[] lines = frame.closure.prototype.lines;
            if (lines != null && (pc = frame.pc - 1) >= 0 && pc < lines.length) {
                return "at " + String.valueOf(frame.closure.prototype) + ":" + lines[pc] + "\n";
            }
        } else {
            return "at " + String.valueOf(frame.javaFunction) + "\n";
        }
        return "";
    }

    public boolean isDead() {
        return this.callFrameTop == 0;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public String getStatus() {
        if (this.parent == null) {
            if (this.isDead()) {
                return "dead";
            }
            return "suspended";
        }
        return "normal";
    }

    public boolean atBottom() {
        return this.callFrameTop == 1;
    }

    public int getCallframeTop() {
        return this.callFrameTop;
    }

    public LuaCallFrame[] getCallframeStack() {
        return this.callFrameStack;
    }

    public LuaCallFrame getCallFrame(int index) {
        if (index < 0) {
            index += this.callFrameTop;
        }
        return this.callFrameStack[index];
    }

    public static void yieldHelper(LuaCallFrame callFrame, LuaCallFrame argsCallFrame, int nArguments) {
        KahluaUtil.luaAssert(callFrame.canYield, "Can not yield outside of a coroutine");
        Coroutine coroutine = callFrame.coroutine;
        KahluaThread thread2 = coroutine.getThread();
        Coroutine parent = coroutine.parent;
        KahluaUtil.luaAssert(parent != null, "Internal error, coroutine must be running");
        KahluaUtil.luaAssert(coroutine == thread2.currentCoroutine, "Internal error, must yield current thread");
        coroutine.destroy();
        LuaCallFrame nextCallFrame = parent.currentCallFrame();
        if (nextCallFrame == null) {
            parent.setTop(nArguments + 1);
            parent.objectStack[0] = Boolean.TRUE;
            for (int i = 0; i < nArguments; ++i) {
                parent.objectStack[i + 1] = argsCallFrame.get(i);
            }
        } else {
            nextCallFrame.push(Boolean.TRUE);
            for (int i = 0; i < nArguments; ++i) {
                Object value = argsCallFrame.get(i);
                nextCallFrame.push(value);
            }
        }
        thread2.currentCoroutine = parent;
    }

    public void resume(Coroutine parent) {
        this.parent = parent;
        this.thread = parent.thread;
    }

    public KahluaThread getThread() {
        return this.thread;
    }

    public void destroy() {
        this.parent = null;
        this.thread = null;
    }
}

