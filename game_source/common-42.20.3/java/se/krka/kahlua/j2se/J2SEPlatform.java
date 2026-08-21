/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.j2se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.j2se.MathLib;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.stdlib.RandomLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.threading.BlockingKahluaThread;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

public class J2SEPlatform
implements Platform {
    private static final J2SEPlatform INSTANCE = new J2SEPlatform();

    public static J2SEPlatform getInstance() {
        return INSTANCE;
    }

    @Override
    public double pow(double x, double y) {
        return Math.pow(x, y);
    }

    @Override
    public KahluaTable newTable() {
        return new KahluaTableImpl(new LinkedHashMap<Object, Object>());
    }

    @Override
    public KahluaTable newEnvironment() {
        KahluaTable env = this.newTable();
        this.setupEnvironment(env);
        return env;
    }

    @Override
    public void setupEnvironment(KahluaTable env) {
        env.wipe();
        env.rawset("_G", (Object)env);
        env.rawset("_VERSION", (Object)"Kahlua kahlua.major.kahlua.minor.kahlua.fix for Lua lua.version (J2SE)");
        MathLib.register(this, env);
        BaseLib.register(env);
        RandomLib.register(this, env);
        UserdataArray.register(this, env);
        StringLib.register(this, env);
        CoroutineLib.register(this, env);
        OsLib.register(this, env);
        TableLib.register(this, env);
        LuaCompiler.register(env);
        KahluaThread workerThread = this.setupWorkerThread(env);
        KahluaUtil.setupLibraryText(env, workerThread, new File("stdlib.lua").getAbsoluteFile());
        File file = new File("serialize.lua").getAbsoluteFile();
        try (FileInputStream fis = new FileInputStream(file);){
            LuaClosure closure = LuaCompiler.loadis(fis, "serialize.lua", env);
            workerThread.call(closure, null, null, null);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private KahluaThread setupWorkerThread(KahluaTable env) {
        BlockingKahluaThread thread2 = new BlockingKahluaThread(this, env);
        KahluaUtil.setWorkerThread(env, thread2);
        return thread2;
    }
}

