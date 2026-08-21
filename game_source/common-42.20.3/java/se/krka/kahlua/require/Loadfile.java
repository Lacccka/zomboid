/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.require;

import java.io.Reader;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.require.LuaSourceProvider;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;

public class Loadfile
implements JavaFunction {
    private final LuaSourceProvider luaSourceProvider;

    public void install(KahluaTable environment) {
        environment.rawset("loadfile", (Object)this);
    }

    public Loadfile(LuaSourceProvider luaSourceProvider) {
        this.luaSourceProvider = luaSourceProvider;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        String path = KahluaUtil.getStringArg(callFrame, 1, "loadfile");
        Reader source2 = this.luaSourceProvider.getLuaSource(path);
        if (source2 == null) {
            callFrame.pushNil();
            callFrame.push("Does not exist: " + path);
            return 2;
        }
        callFrame.setTop(2);
        callFrame.set(0, source2);
        callFrame.set(1, path);
        return LuaCompiler.loadstream(callFrame, 2);
    }
}

