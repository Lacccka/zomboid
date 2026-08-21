/*
 * Decompiled with CFR 0.152.
 */
package zombie.debug;

import java.io.PrintStream;
import zombie.UsedFromLua;
import zombie.debug.DebugLog;
import zombie.debug.DebugLogStream;
import zombie.debug.IDebugLogFormatter;
import zombie.debug.LogSeverity;
import zombie.util.StringUtils;

@UsedFromLua
public enum DebugType {
    General(new DebugType[0]),
    Packet(new DebugType[0]),
    NetworkFileDebug(new DebugType[0]),
    Network(new DebugType[0]),
    ZNet(new DebugType[0]),
    DetailedInfo(new DebugType[0]),
    Lua(new DebugType[0]),
    LuaObject(new DebugType[0]),
    GameOption(new DebugType[0]),
    Mod(new DebugType[0]),
    Sound(new DebugType[0]),
    Zombie(new DebugType[0]),
    Combat(new DebugType[0]),
    Objects(new DebugType[0]),
    Fireplace(new DebugType[0]),
    Radio(new DebugType[0]),
    MapLoading(new DebugType[0]),
    Clothing(new DebugType[0]),
    Animation(new DebugType[0]),
    AnimationDetailed(new DebugType[0]),
    AnimationLayers(new DebugType[0]),
    Asset(new DebugType[0]),
    Script(new DebugType[0]),
    Shader(new DebugType[0]),
    Sprite(new DebugType[0]),
    Input(new DebugType[0]),
    Recipe(new DebugType[0]),
    ActionSystem(new DebugType[0]),
    ActionSystemEvents(new DebugType[0]),
    IsoRegion(new DebugType[0]),
    FileIO(new DebugType[0]),
    Multiplayer(new DebugType[0]),
    Damage(new DebugType[0]),
    Death(new DebugType[0]),
    Discord(new DebugType[0]),
    Statistic(new DebugType[0]),
    Vehicle(new DebugType[0]),
    VehicleHit(new DebugType[0]),
    Voice(new DebugType[0]),
    Checksum(new DebugType[0]),
    Animal(new DebugType[0]),
    ItemPicker(new DebugType[0]),
    CraftLogic(new DebugType[0]),
    Action(new DebugType[0]),
    Entity(new DebugType[0]),
    Lightning(new DebugType[0]),
    Grapple(new DebugType[0]),
    ExitDebug(new DebugType[0]),
    BodyDamage(new DebugType[0]),
    Xml(new DebugType[0]),
    Physics(new DebugType[0]),
    Ballistics(new DebugType[0]),
    Ragdoll(new DebugType[0]),
    PZBullet(new DebugType[0]),
    ModelManager(new DebugType[0]),
    LoadAnimation(new DebugType[0]),
    Zone(new DebugType[0]),
    WorldGen(new DebugType[0]),
    Foraging(new DebugType[0]),
    Saving(new DebugType[0]),
    Fluid(new DebugType[0]),
    Energy(new DebugType[0]),
    Translation(new DebugType[0]),
    Moveable(new DebugType[0]),
    Basement(new DebugType[0]),
    FallDamage(new DebugType[0]),
    ImGui(new DebugType[0]),
    CharacterTrait(new DebugType[0]),
    ISUI(new DebugType[0]),
    ISUIStackTrace(new DebugType[0]),
    FaceLocationFix(new DebugType[0]),
    Context(new DebugType[0]),
    AnimationRecorder(General, Animation, ActionSystem, ActionSystemEvents, Death, Vehicle, VehicleHit, Ragdoll, Context);

    public static final DebugType Default;
    private LogSeverity logSeverity = LogSeverity.Off;
    private DebugType orWith;
    private volatile DebugLogStream logStream = null;
    private final Object logStreamLock = new Object();
    private final IDebugLogFormatter formatter = this::formatLogStringForConsole;

    private DebugType(DebugType ... alsoActivate) {
        for (DebugType toBind : alsoActivate) {
            toBind.orWith = this;
        }
    }

    public boolean isName(String rhs) {
        return StringUtils.equalsIgnoreCase(this.name(), rhs);
    }

    public boolean isEnabled() {
        return this.logSeverity != LogSeverity.Off || this.orWith != null && this.orWith.isEnabled();
    }

    public boolean isEnabled(LogSeverity logSeverity) {
        return this.logSeverity.isLogEnabled(logSeverity) || this.orWith != null && this.orWith.isEnabled(logSeverity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DebugLogStream getLogStream() {
        if (this.logStream == null) {
            Object object = this.logStreamLock;
            synchronized (object) {
                this.logStream = DebugLog.getInstance().createLogStream(this);
            }
        }
        return this.logStream;
    }

    private String formatLogStringForConsole(DebugType debugType, LogSeverity logSeverity, String affix, Object outputString) {
        return DebugLog.getInstance().formatLogStringForConsole(debugType, logSeverity, affix, outputString);
    }

    public void setLogSeverity(LogSeverity newSeverity) {
        this.logSeverity = newSeverity;
    }

    public LogSeverity getLogSeverity() {
        return this.logSeverity;
    }

    public IDebugLogFormatter getFormatter() {
        return this.formatter;
    }

    public void print(boolean b) {
        this.getLogStream().print(b);
    }

    public void print(char c) {
        this.getLogStream().print(c);
    }

    public void print(int i) {
        this.getLogStream().print(i);
    }

    public void print(long l) {
        this.getLogStream().print(l);
    }

    public void print(float f) {
        this.getLogStream().print(f);
    }

    public void print(double d) {
        this.getLogStream().print(d);
    }

    public void print(String s) {
        this.getLogStream().print(s);
    }

    public void print(Object obj) {
        this.getLogStream().print(obj);
    }

    public PrintStream printf(String format, Object ... args2) {
        return this.getLogStream().printf(format, args2);
    }

    public void println() {
        this.getLogStream().println();
    }

    public void println(boolean x) {
        this.getLogStream().println(x);
    }

    public void println(char x) {
        this.getLogStream().println(x);
    }

    public void println(int x) {
        this.getLogStream().println(x);
    }

    public void println(long x) {
        this.getLogStream().println(x);
    }

    public void println(float x) {
        this.getLogStream().println(x);
    }

    public void println(double x) {
        this.getLogStream().println(x);
    }

    public void println(char[] x) {
        this.getLogStream().println(x);
    }

    public void println(String x) {
        this.getLogStream().println(x);
    }

    public void println(Object x) {
        this.getLogStream().println(x);
    }

    public void println(String format, Object ... params) {
        this.getLogStream().println(format, params);
    }

    public void trace(Object formatNoParams) {
        this.getLogStream().traceWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void trace(String format, Object ... params) {
        this.getLogStream().traceWithTraceOffset(1, format, params);
    }

    public void debugln(Object formatNoParams) {
        this.getLogStream().debuglnWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void debugln(String format, Object ... params) {
        this.getLogStream().debuglnWithTraceOffset(1, format, params);
    }

    public void debugOnceln(Object formatNoParams) {
        this.getLogStream().debugOncelnWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void debugOnceln(String format, Object ... params) {
        this.getLogStream().debugOncelnWithTraceOffset(1, format, params);
    }

    public void noise(Object formatNoParams) {
        this.getLogStream().noiseWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void noise(String format, Object ... params) {
        this.getLogStream().noiseWithTraceOffset(1, format, params);
    }

    public void warn(Object formatNoParams) {
        this.getLogStream().warnWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void warn(String format, Object ... params) {
        this.getLogStream().warnWithTraceOffset(1, format, params);
    }

    public void warnOnce(Object formatNoParams) {
        this.getLogStream().warnOnceWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void warnOnce(String format, Object ... params) {
        this.getLogStream().warnOnceWithTraceOffset(1, format, params);
    }

    public void error(Object formatNoParams) {
        this.getLogStream().errorWithTraceOffset(1, formatNoParams, new Object[0]);
    }

    public void error(String format, Object ... params) {
        this.getLogStream().errorWithTraceOffset(1, format, params);
    }

    public void write(LogSeverity logSeverity, String logText) {
        this.routedWrite(1, logSeverity, logText);
    }

    public void routedWrite(int backTraceOffset, LogSeverity logSeverity, String logText) {
        switch (logSeverity) {
            case Trace: {
                this.getLogStream().traceWithTraceOffset(backTraceOffset + 1, logText, new Object[0]);
                break;
            }
            case Noise: {
                this.getLogStream().noiseWithTraceOffset(backTraceOffset + 1, logText, new Object[0]);
                break;
            }
            case Debug: {
                this.getLogStream().debuglnWithTraceOffset(backTraceOffset + 1, logText, new Object[0]);
                break;
            }
            case General: {
                this.getLogStream().println(logText);
                break;
            }
            case Warning: {
                this.getLogStream().warnWithTraceOffset(backTraceOffset + 1, logText, new Object[0]);
                break;
            }
            case Error: {
                this.getLogStream().errorWithTraceOffset(backTraceOffset + 1, logText, new Object[0]);
                break;
            }
        }
    }

    public void printException(Throwable ex, LogSeverity logSeverity) {
        this.getLogStream().printException(ex, null, DebugLogStream.generateCallerPrefix(), logSeverity);
    }

    public void printException(Throwable ex, String message, LogSeverity logSeverity) {
        this.getLogStream().printException(ex, message, DebugLogStream.generateCallerPrefix(), logSeverity);
    }

    public void printException(Throwable ex, LogSeverity logSeverity, String messageFormat, Object ... params) {
        this.getLogStream().printException(ex, logSeverity, DebugLogStream.generateCallerPrefix(), messageFormat, new Object[]{logSeverity});
    }

    public void printStackTrace() {
        this.getLogStream().printStackTrace(LogSeverity.Error, 1, -1, null, new Object[0]);
    }

    public void printStackTrace(String message) {
        this.getLogStream().printStackTrace(LogSeverity.Error, 1, -1, message, new Object[0]);
    }

    public void printStackTrace(LogSeverity severity, int depth, String messageFormat, Object ... params) {
        this.getLogStream().printStackTrace(severity, 1, depth, messageFormat, params);
    }

    static {
        Default = General;
    }
}

