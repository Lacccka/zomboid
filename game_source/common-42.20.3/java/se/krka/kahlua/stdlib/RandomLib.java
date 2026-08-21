/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.stdlib;

import java.util.Random;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.Platform;

public class RandomLib
implements JavaFunction {
    private static final Class<?> RANDOM_CLASS = new Random().getClass();
    private static final int RANDOM = 0;
    private static final int RANDOMSEED = 1;
    private static final int NEWRANDOM = 2;
    private static final int NUM_FUNCTIONS = 3;
    private static final String[] names = new String[3];
    private static final RandomLib[] functions;
    private static final RandomLib NEWRANDOM_FUN;
    private final int index;

    public RandomLib(int index) {
        this.index = index;
    }

    public static void register(Platform platform, KahluaTable environment) {
        KahluaTable t = platform.newTable();
        for (int i = 0; i < 2; ++i) {
            t.rawset(names[i], (Object)functions[i]);
        }
        t.rawset("__index", (Object)t);
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, environment);
        metatables.rawset(RANDOM_CLASS, (Object)t);
        environment.rawset("newrandom", (Object)NEWRANDOM_FUN);
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (this.index) {
            case 0: {
                return this.random(callFrame, nArguments);
            }
            case 1: {
                return this.randomSeed(callFrame, nArguments);
            }
            case 2: {
                return this.newRandom(callFrame);
            }
        }
        return 0;
    }

    private int randomSeed(LuaCallFrame callFrame, int nArguments) {
        Random random = this.getRandom(callFrame, "seed");
        Object o = callFrame.get(1);
        int hashCode = o == null ? 0 : o.hashCode();
        random.setSeed(hashCode);
        return 0;
    }

    private int random(LuaCallFrame callFrame, int nArguments) {
        int n;
        Random random = this.getRandom(callFrame, "random");
        Double min = KahluaUtil.getOptionalNumberArg(callFrame, 2);
        Double max = KahluaUtil.getOptionalNumberArg(callFrame, 3);
        if (min == null) {
            return callFrame.push(KahluaUtil.toDouble(random.nextDouble()));
        }
        int m = min.intValue();
        if (max == null) {
            n = m;
            m = 1;
        } else {
            n = max.intValue();
        }
        return callFrame.push(KahluaUtil.toDouble(m + random.nextInt(n - m + 1)));
    }

    private Random getRandom(LuaCallFrame callFrame, String name) {
        Object obj = KahluaUtil.getArg(callFrame, 1, name);
        if (!(obj instanceof Random)) {
            KahluaUtil.fail("First argument to " + name + " must be an object of type random.");
        }
        return (Random)obj;
    }

    private int newRandom(LuaCallFrame callFrame) {
        return callFrame.push(new Random());
    }

    static {
        RandomLib.names[0] = "random";
        RandomLib.names[1] = "seed";
        RandomLib.names[2] = "newrandom";
        functions = new RandomLib[3];
        for (int i = 0; i < 3; ++i) {
            RandomLib.functions[i] = new RandomLib(i);
        }
        NEWRANDOM_FUN = new RandomLib(2);
    }
}

