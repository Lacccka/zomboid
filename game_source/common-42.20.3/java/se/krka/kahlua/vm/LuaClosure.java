/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.File;
import java.util.Objects;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.Prototype;
import se.krka.kahlua.vm.UpValue;
import zombie.Lua.LuaEventManager;
import zombie.Lua.LuaManager;
import zombie.Lua.MapObjects;
import zombie.ZomboidFileSystem;
import zombie.core.math.PZMath;
import zombie.gameStates.ChooseGameInfo;
import zombie.util.StringUtils;
import zombie.util.list.PZArrayUtil;

public final class LuaClosure {
    public final Prototype prototype;
    public KahluaTable env;
    public final UpValue[] upvalues;
    public String debugName;
    private DebugInfo debugInfo;

    public LuaClosure(Prototype prototype, KahluaTable env) {
        this.prototype = prototype;
        if (LuaCompiler.rewriteEvents) {
            LuaEventManager.reroute(prototype, this);
            MapObjects.reroute(prototype, this);
        }
        this.env = env;
        this.upvalues = new UpValue[prototype.numUpvalues];
    }

    public String toString() {
        if (this.prototype.lines.length > 0) {
            return "function " + String.valueOf(this.prototype) + ":" + this.prototype.lines[0];
        }
        return "function[" + Integer.toString(this.hashCode(), 36) + "]";
    }

    public String toString2(int line) {
        if (this.prototype.lines.length > 0) {
            if (line == 0) {
                line = 1;
            }
            if (this.isBuiltInFunction()) {
                return "function: " + this.prototype.name + " -- file: " + this.prototype.file + " line # " + this.prototype.lines[line - 1];
            }
            Object sourceName = " | Vanilla";
            String val = this.prototype.filename;
            if ((val = val.replace("/", File.separator)).contains(File.separator + "mods" + File.separator)) {
                String dir = val.substring(0, val.indexOf(File.separator + "media"));
                String dir1 = dir.substring(0, dir.lastIndexOf(File.separator));
                ChooseGameInfo.Mod mod = ZomboidFileSystem.instance.getModInfoForDir(dir1);
                sourceName = " | MOD: " + mod.getName();
                KahluaTable tab = (KahluaTable)LuaManager.env.rawget("PauseBuggedModList");
                if (tab != null) {
                    tab.rawset(mod.getName(), (Object)true);
                }
            }
            return "function: " + this.prototype.name + " -- file: " + this.prototype.file + " line # " + this.prototype.lines[line - 1] + (String)sourceName;
        }
        return "function[" + Integer.toString(this.hashCode(), 36) + "]";
    }

    public StackTraceElement toStackTraceElement(int line) {
        return new StackTraceElement(this.getDebugInfo().getSourceName(), this.getDebugInfo().getFunctionName(), this.prototype.file, PZArrayUtil.getOrDefault(this.prototype.lines, PZMath.max(line - 1, 0), 0));
    }

    private boolean isBuiltInFunction() {
        return this.prototype.lines.length > 0 && this.prototype.filename == null;
    }

    private DebugInfo getDebugInfo() {
        this.debugInfo = Objects.requireNonNullElse(this.debugInfo, new DebugInfo(this));
        return this.debugInfo;
    }

    private class DebugInfo {
        private String rawFileName;
        private String rawPrototypeName;
        private int rawLineCount;
        private String fileName;
        private String functionName;
        private String sourceName;
        private String modName;
        final /* synthetic */ LuaClosure this$0;

        private DebugInfo(LuaClosure luaClosure) {
            LuaClosure luaClosure2 = luaClosure;
            Objects.requireNonNull(luaClosure2);
            this.this$0 = luaClosure2;
        }

        private boolean isDirty() {
            return !StringUtils.equals(this.rawFileName, this.getRawFileName()) || !StringUtils.equals(this.rawPrototypeName, this.getRawPrototypeName()) || this.rawLineCount != PZArrayUtil.lengthOf(this.this$0.prototype.lines);
        }

        private void updateDirty() {
            if (this.isDirty()) {
                this.fileName = this.generateFileName();
                this.functionName = this.generateFunctionName();
                this.modName = this.generateModName();
                this.sourceName = "Lua(" + this.modName + ")";
                this.rawFileName = this.getRawFileName();
                this.rawPrototypeName = this.getRawPrototypeName();
                this.rawLineCount = PZArrayUtil.lengthOf(this.this$0.prototype.lines);
            }
        }

        public String getSourceName() {
            this.updateDirty();
            return this.sourceName;
        }

        public String getModName() {
            this.updateDirty();
            return this.modName;
        }

        public String getFunctionName() {
            this.updateDirty();
            return this.functionName;
        }

        private String generateFileName() {
            return this.getRawFileName().replace("/", File.separator);
        }

        private String generateFunctionName() {
            if (this.this$0.isBuiltInFunction()) {
                return this.this$0.prototype.name;
            }
            if (PZArrayUtil.lengthOf(this.this$0.prototype.lines) == 0) {
                return "function[" + Integer.toString(this.hashCode(), 36) + "]";
            }
            String functionName = this.this$0.prototype.name;
            if (functionName == null) {
                functionName = this.fileName;
            }
            if (functionName.equals(this.fileName)) {
                int lastIndexOfDot;
                int lastIndexOfSlash = functionName.lastIndexOf(File.separator);
                if (lastIndexOfSlash != -1) {
                    functionName = functionName.substring(lastIndexOfSlash + 1);
                }
                if ((lastIndexOfDot = functionName.lastIndexOf(46)) != -1) {
                    functionName = functionName.substring(0, lastIndexOfDot);
                }
            }
            return functionName;
        }

        private String generateModName() {
            if (this.this$0.isBuiltInFunction()) {
                return "BuiltIn";
            }
            Object sourceName = "Vanilla";
            if (this.fileName.contains(File.separator + "mods" + File.separator)) {
                String dir = this.fileName.substring(0, this.fileName.indexOf(File.separator + "media"));
                String dir1 = dir.substring(0, dir.lastIndexOf(File.separator));
                ChooseGameInfo.Mod mod = ZomboidFileSystem.instance.getModInfoForDir(dir1);
                sourceName = "(MOD:" + mod.getName() + ")";
                KahluaTable tab = (KahluaTable)LuaManager.env.rawget("PauseBuggedModList");
                if (tab != null) {
                    tab.rawset(mod.getName(), (Object)true);
                }
            }
            return sourceName;
        }

        private String getRawFileName() {
            return this.this$0.prototype.filename != null ? this.this$0.prototype.filename : Objects.requireNonNullElse(this.this$0.prototype.file, "(Unknown File)");
        }

        private String getRawPrototypeName() {
            return this.this$0.prototype.name;
        }
    }
}

