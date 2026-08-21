/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.require;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.require.LuaSourceProvider;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;

public class Require
implements JavaFunction {
    private final LuaSourceProvider luaSourceProvider;

    public void install(KahluaTable environment) {
        environment.rawset("require", (Object)this);
        environment.rawset(this, new HashMap());
    }

    public Require(LuaSourceProvider luaSourceProvider) {
        this.luaSourceProvider = luaSourceProvider;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        String path;
        KahluaTable env = callFrame.getEnvironment();
        Map states = (Map)callFrame.getThread().tableget(env, this);
        Result result = (Result)states.get(path = KahluaUtil.getStringArg(callFrame, 1, "require"));
        if (result == null) {
            this.setState(states, path, Result.LOADING);
            Reader source2 = this.luaSourceProvider.getLuaSource(path);
            if (source2 == null) {
                this.error(states, path, "Does not exist: " + path);
            }
            try {
                LuaClosure luaClosure = LuaCompiler.loadis(source2, path, env);
                this.setState(states, path, Result.LOADING);
                callFrame.getThread().call(luaClosure, null, null, null);
                this.setState(states, path, Result.LOADED);
                return 0;
            }
            catch (IOException e) {
                this.error(states, path, "Error in: " + path + ": " + e.getMessage());
            }
            catch (RuntimeException e) {
                String msg = "Error in: " + path + ": " + e.getMessage();
                this.setState(states, path, Result.error(msg));
                throw new RuntimeException(msg, e);
            }
        }
        if (result == Result.LOADING) {
            this.error(states, path, "Circular dependency found for: " + path);
        }
        if (result.state == State.BROKEN) {
            KahluaUtil.fail(result.errorMessage);
        }
        return 0;
    }

    private void error(Map<String, Result> states, String path, String s) {
        this.setState(states, path, Result.error(s));
        KahluaUtil.fail(s);
    }

    private void setState(Map<String, Result> requireLookuptable, String path, Result result) {
        requireLookuptable.put(path, result);
    }

    private static class Result {
        public final String errorMessage;
        public final State state;
        public static final Result LOADING = new Result(null, State.LOADING);
        public static final Result LOADED = new Result(null, State.LOADED);

        private Result(String errorMessage, State state) {
            this.errorMessage = errorMessage;
            this.state = state;
        }

        public static Result error(String s) {
            return new Result(s, State.BROKEN);
        }
    }

    private static enum State {
        LOADING,
        LOADED,
        BROKEN;

    }
}

