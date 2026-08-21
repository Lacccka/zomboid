/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.util.ArrayList;
import se.krka.kahlua.integration.expose.ReturnValues;
import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.math.PZMath;
import zombie.debug.DebugOptions;
import zombie.ui.UIManager;
import zombie.util.StringUtils;

public class MethodArguments {
    private ReturnValues returnValues;
    private Object self;
    private final Object[] params;
    private String failure;
    private boolean valid = true;
    private static final int MAX_PARAMS = 30;
    private static final int INITIAL_POOL_SIZE = 20;
    private static final ArrayList<MethodArguments>[] Pools = new ArrayList[30];
    private static final int[] minPoolSize = new int[30];

    public static MethodArguments get(int params) {
        MethodArguments a;
        ArrayList<MethodArguments> pool = Pools[params];
        if (pool.isEmpty()) {
            a = new MethodArguments(params);
        } else {
            a = pool.remove(pool.size() - 1);
            MethodArguments.minPoolSize[params] = PZMath.min(minPoolSize[params], pool.size());
        }
        return a;
    }

    public static void put(MethodArguments a) {
        a.valid = true;
        a.self = null;
        a.failure = null;
        a.returnValues = null;
        for (int n = 0; n < a.params.length; ++n) {
            a.params[n] = null;
        }
        if (DebugOptions.instance.checks.objectPoolContains.getValue() && Pools[a.params.length].contains(a)) {
            throw new RuntimeException("object pool already contains object");
        }
        Pools[a.params.length].add(a);
    }

    public MethodArguments(int numMethodParams) {
        this.params = new Object[numMethodParams];
    }

    public ReturnValues getReturnValues() {
        return this.returnValues;
    }

    public Object getSelf() {
        return this.self;
    }

    public Object[] getParams() {
        return this.params;
    }

    public void fail(String failure) {
        this.failure = failure;
        this.valid = false;
    }

    public void setSelf(Object self) {
        this.self = self;
    }

    public String getFailure() {
        return this.failure;
    }

    public void setReturnValues(ReturnValues returnValues) {
        this.returnValues = returnValues;
    }

    public void assertValid() {
        if (!this.isValid()) {
            if (UIManager.defaultthread == LuaManager.thread) {
                if (!StringUtils.isNullOrWhitespace(this.getFailure())) {
                    StringBuilder sb = LuaManager.thread.startErrorMessage();
                    sb.append(this.getFailure());
                    LuaManager.thread.flushErrorMessage();
                }
                LuaManager.thread.doStacktraceProper();
                if (Core.debug) {
                    UIManager.debugBreakpoint(LuaManager.thread.currentfile, LuaManager.thread.currentLine - 1);
                }
            }
            throw new RuntimeException(this.failure);
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    static {
        for (int n = 0; n < 30; ++n) {
            MethodArguments.Pools[n] = new ArrayList(20);
            MethodArguments.minPoolSize[n] = 20;
            for (int x = 0; x < 20; ++x) {
                Pools[n].add(new MethodArguments(n));
            }
        }
    }
}

