/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.luaj.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.luaj.kahluafork.compiler.LexState;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;

public class LuaCompiler
implements JavaFunction {
    public static boolean rewriteEvents;
    private final int index;
    private static final int LOADSTRING = 0;
    private static final int LOADSTREAM = 1;
    private static final String[] names;
    private static final LuaCompiler[] functions;

    private LuaCompiler(int index) {
        this.index = index;
    }

    public static void register(KahluaTable env) {
        for (int i = 0; i < names.length; ++i) {
            env.rawset(names[i], (Object)functions[i]);
        }
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (this.index) {
            case 0: {
                return this.loadstring(callFrame, nArguments);
            }
            case 1: {
                return LuaCompiler.loadstream(callFrame, nArguments);
            }
        }
        return 0;
    }

    public static int loadstream(LuaCallFrame callFrame, int nArguments) {
        try {
            KahluaUtil.luaAssert(nArguments >= 2, "not enough arguments");
            Object input = callFrame.get(0);
            KahluaUtil.luaAssert(input != null, "No input given");
            String name = (String)callFrame.get(1);
            if (input instanceof Reader) {
                Reader reader = (Reader)input;
                return callFrame.push(LuaCompiler.loadis(reader, name, null, callFrame.getEnvironment()));
            }
            if (input instanceof InputStream) {
                InputStream inputStream2 = (InputStream)input;
                return callFrame.push(LuaCompiler.loadis(inputStream2, name, null, callFrame.getEnvironment()));
            }
            KahluaUtil.fail("Invalid type to loadstream: " + String.valueOf(input.getClass()));
            return 0;
        }
        catch (IOException | RuntimeException e) {
            return callFrame.push(null, e.getMessage());
        }
    }

    private int loadstring(LuaCallFrame callFrame, int nArguments) {
        try {
            KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
            String source2 = (String)callFrame.get(0);
            KahluaUtil.luaAssert(source2 != null, "No source given");
            String name = null;
            if (nArguments >= 2) {
                name = (String)callFrame.get(1);
            }
            return callFrame.push(LuaCompiler.loadstring(source2, name, callFrame.getEnvironment()));
        }
        catch (IOException | RuntimeException e) {
            return callFrame.push(null, e.getMessage());
        }
    }

    public static LuaClosure loadis(InputStream inputStream2, String name, KahluaTable environment) throws IOException {
        return LuaCompiler.loadis(inputStream2, name, null, environment);
    }

    public static LuaClosure loadis(Reader reader, String name, KahluaTable environment) throws IOException {
        return LuaCompiler.loadis(reader, name, null, environment);
    }

    public static LuaClosure loadstring(String source2, String name, KahluaTable environment) throws IOException {
        return LuaCompiler.loadis(new ByteArrayInputStream(source2.getBytes("UTF-8")), name, source2, environment);
    }

    private static LuaClosure loadis(Reader reader, String name, String source2, KahluaTable environment) throws IOException {
        return new LuaClosure(LexState.compile(reader.read(), reader, name, source2), environment);
    }

    private static LuaClosure loadis(InputStream inputStream2, String name, String source2, KahluaTable environment) throws IOException {
        return LuaCompiler.loadis(new InputStreamReader(inputStream2), name, source2, environment);
    }

    static {
        names = new String[]{"loadstring", "loadstream"};
        functions = new LuaCompiler[names.length];
        for (int i = 0; i < names.length; ++i) {
            LuaCompiler.functions[i] = new LuaCompiler(i);
        }
    }
}

