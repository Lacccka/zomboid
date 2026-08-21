/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.util.ArrayList;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.vm.LuaCallFrame;
import zombie.core.math.PZMath;
import zombie.debug.DebugOptions;

public class ReturnValues {
    private KahluaConverterManager manager;
    private LuaCallFrame callFrame;
    private int args;
    private static final int INITIAL_POOL_SIZE = 20;
    private static final ArrayList<ReturnValues> Pool = new ArrayList(20);
    private static int minPoolSize;

    public static ReturnValues get(KahluaConverterManager manager, LuaCallFrame callFrame) {
        ReturnValues a;
        if (Pool.isEmpty()) {
            a = new ReturnValues(manager, callFrame);
        } else {
            a = Pool.remove(Pool.size() - 1);
            minPoolSize = PZMath.min(minPoolSize, Pool.size());
        }
        a.manager = manager;
        a.callFrame = callFrame;
        return a;
    }

    public static void put(ReturnValues a) {
        a.callFrame = null;
        a.manager = null;
        a.args = 0;
        if (DebugOptions.instance.checks.objectPoolContains.getValue() && Pool.contains(a)) {
            throw new RuntimeException("object pool already contains object");
        }
        Pool.add(a);
    }

    ReturnValues(KahluaConverterManager manager, LuaCallFrame callFrame) {
        this.manager = manager;
        this.callFrame = callFrame;
    }

    public ReturnValues push(Object obj) {
        this.args += this.callFrame.push(this.manager.fromJavaToLua(obj));
        return this;
    }

    public ReturnValues push(Object ... objects) {
        for (Object o : objects) {
            this.push(o);
        }
        return this;
    }

    int getNArguments() {
        return this.args;
    }

    static {
        for (int x = 0; x < 20; ++x) {
            Pool.add(new ReturnValues(null, null));
        }
        minPoolSize = Pool.size();
    }
}

