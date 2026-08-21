/*
 * Decompiled with CFR 0.152.
 */
package zombie.debug;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.StreamSet;
import pl.mjaron.tinyloki.TinyLoki;
import zombie.DebugFileWatcher;
import zombie.GameTime;
import zombie.PredicatedFileWatcher;
import zombie.UsedFromLua;
import zombie.ZomboidFileSystem;
import zombie.core.Core;
import zombie.core.logger.LoggerManager;
import zombie.core.logger.ZLogger;
import zombie.debug.DebugLogCfgFile;
import zombie.debug.DebugLogProfile;
import zombie.debug.DebugLogStream;
import zombie.debug.DebugOptions;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.iso.IsoWorld;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.statistics.StatisticManager;
import zombie.scripting.ScriptManager;
import zombie.scripting.ScriptType;
import zombie.ui.UIDebugConsole;
import zombie.util.StringUtils;
import zombie.util.lambda.PZOptional;
import zombie.util.list.PZArrayUtil;

@UsedFromLua
public final class DebugLog {
    private static volatile DebugLog instance;
    private static final Object instanceLock;
    private boolean initialized;
    private boolean logTraceFileLocationEnabled;
    private boolean logTimeMsEnabled;
    private boolean logServerTimeMsEnabled;
    private final OutputStreamWrapper stdOut = new OutputStreamWrapper(System.out);
    private final OutputStreamWrapper stdErr = new OutputStreamWrapper(System.err);
    private final PrintStream originalOut = new PrintStream(this.stdOut, true);
    private final PrintStream originalErr = new PrintStream(this.stdErr, true);
    private final PrintStream GeneralErr = new DebugLogStream(this.originalErr, this.originalErr, this.originalErr, LogSeverity.All);
    private ZLogger logFileLogger;
    private final DebugLogCfgFile dbgCfgFile = new DebugLogCfgFile();
    private PredicatedFileWatcher debugCfgFileWatcher;
    private String debugCfgFileWatcherPath;
    private boolean lokiInit;
    private TinyLoki loki;
    private StreamSet logSet;
    private ILogStream errorStream;
    private PrintStream recordingOut;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DebugLog getInstance() {
        if (instance == null) {
            Object object = instanceLock;
            synchronized (object) {
                if (instance == null) {
                    instance = new DebugLog();
                }
            }
        }
        return instance;
    }

    private DebugLog() {
    }

    public static void setDefaultLogSeverity() {
        LogSeverity logSeverity = DebugLog.getDefaultLogSeverity();
        for (DebugType debugType : DebugType.values()) {
            if (logSeverity.ordinal() >= debugType.getLogSeverity().ordinal()) continue;
            debugType.setLogSeverity(logSeverity);
        }
    }

    private static LogSeverity getDefaultLogSeverity() {
        if (Core.debug) {
            return LogSeverity.General;
        }
        if (GameServer.server) {
            return LogSeverity.Warning;
        }
        return LogSeverity.Off;
    }

    public static void printLogLevels() {
        DebugType.DetailedInfo.trace("You can setup the log levels in the " + DebugLog.getConfigFilePath() + " file");
        DebugType.General.println("Logs configuration:");
        for (LogSeverity logSeverity : LogSeverity.values()) {
            if (logSeverity == LogSeverity.Off) continue;
            for (DebugType type : PZArrayUtil.filtered(DebugType.values(), t -> t.getLogSeverity() == logSeverity)) {
                DebugType.General.println("%12s: %s", new Object[]{type.name(), type.getLogSeverity()});
            }
        }
        LogSeverity defaultLogSeverity = DebugLog.getDefaultLogSeverity();
        DebugType.General.println("%12s: %s", new Object[]{"Default", defaultLogSeverity});
    }

    public static boolean isEnabled(DebugType type) {
        return type.isEnabled();
    }

    public static boolean isLogEnabled(DebugType type, LogSeverity logSeverity) {
        return type.isEnabled(logSeverity);
    }

    public String formatLogStringForConsole(DebugType debugType, LogSeverity logSeverity, String callerAffix, Object outputStr) {
        return String.format("%s%s%s%s%s%s> %s", logSeverity.logPrefix, StringUtils.leftJustify(PZOptional.ifPresent(debugType, DebugType.General.toString(), Enum::toString), 12), " f:" + IsoWorld.instance.getFrameNo(), this.shouldLogIncludeTimeMs() ? " t:" + DebugLog.generateCurrentTimeMillisStr() : "", this.shouldLogIncludeServerTime() ? " st:" + DebugLog.generateCurrentServerTimeMillisStr() : "", !StringUtils.isNullOrEmpty(callerAffix) ? " at " + callerAffix : "", outputStr);
    }

    public String formatLogStringForLogFile(DebugType debugType, LogSeverity logSeverity, String callerAffix, Object outputStr) {
        return String.format("%s%s%s%s%s%s> %s", logSeverity.logPrefix, StringUtils.leftJustify(PZOptional.ifPresent(debugType, DebugType.General.toString(), Enum::toString), 12), " f:" + IsoWorld.instance.getFrameNo(), this.shouldLogIncludeTimeMs() ? " t:" + DebugLog.generateCurrentTimeMillisStr() : "", this.shouldLogIncludeServerTime() ? " st:" + DebugLog.generateCurrentServerTimeMillisStr() : "", !StringUtils.isNullOrEmpty(callerAffix) ? " at " + callerAffix : "", outputStr);
    }

    public String formatLogStringAnimationRecordingFile(DebugType debugType, LogSeverity logSeverity, String callerAffix, Object outputStr) {
        return String.format("%s,%s,%s,\"%s%s%s> %s\"", new Object[]{IsoWorld.instance.getFrameNo(), Objects.requireNonNullElse(debugType, DebugType.General), logSeverity, logSeverity.logPrefix, StringUtils.leftJustify(Objects.requireNonNullElse(debugType, DebugType.General).toString(), 12), !StringUtils.isNullOrEmpty(callerAffix) ? " at " + callerAffix : "", String.valueOf(outputStr).replaceAll("\r\n", "\n").replaceAll("\n", "%0A").replaceAll("\"", "%22")});
    }

    private static String generateCurrentTimeMillisStr() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String generateCurrentServerTimeMillisStr() {
        return NumberFormat.getNumberInstance().format(TimeUnit.NANOSECONDS.toMillis(GameTime.getServerTime()));
    }

    public void echoToLogFiles(DebugType debugType, LogSeverity logSeverity, String callerAffix, String rawOutString) {
        String logFileString = this.formatLogStringForLogFile(debugType, logSeverity, callerAffix, rawOutString);
        this.echoToLogFile(logFileString);
        this.echoToLoki(logSeverity, logFileString);
        this.echoToRecording(debugType, logSeverity, callerAffix, rawOutString);
    }

    public void echoExceptionLineToLogFiles(DebugType debugType, LogSeverity logSeverity, String messageType, String outString) {
        String logFileString = this.formatLogStringForLogFile(debugType, logSeverity, "", outString);
        this.echoToLogFile(logFileString);
        this.echoExceptionLineToLoki(logSeverity, messageType, logFileString);
        this.echoToRecording(debugType, logSeverity, "", outString);
    }

    private void echoToLoki(LogSeverity logSeverity, String formattedString) {
        if (this.logSet == null) {
            return;
        }
        switch (logSeverity) {
            case Trace: 
            case Noise: {
                this.logSet.verbose(formattedString);
                break;
            }
            case Debug: {
                this.logSet.debug(formattedString);
                break;
            }
            case General: {
                this.logSet.info(formattedString);
                break;
            }
            case Warning: {
                this.logSet.warning(formattedString);
                break;
            }
            case Error: {
                if (this.errorStream == null) {
                    this.errorStream = this.loki.stream().l("level", "error").open();
                }
                this.errorStream.log(formattedString);
                break;
            }
            default: {
                this.logSet.unknown(formattedString);
            }
        }
    }

    private void echoExceptionLineToLoki(LogSeverity logSeverity, String messageType, String message) {
        if (this.logSet == null) {
            return;
        }
        switch (logSeverity) {
            case Trace: 
            case Noise: {
                this.logSet.verbose(message, Labels.of("type", messageType));
                break;
            }
            case Debug: {
                this.logSet.debug(message, Labels.of("type", messageType));
                break;
            }
            case General: {
                this.logSet.info(message, Labels.of("type", messageType));
                break;
            }
            case Warning: {
                this.logSet.warning(message, Labels.of("type", messageType));
                break;
            }
            case Error: {
                this.logSet.fatal().log(message, Labels.of("type", messageType));
                break;
            }
            default: {
                this.logSet.unknown(message, Labels.of("type", messageType));
            }
        }
    }

    private void echoToLogFile(String formattedLine) {
        if (this.logFileLogger == null) {
            if (this.initialized) {
                return;
            }
            this.logFileLogger = new ZLogger(GameServer.server ? "DebugLog-server" : "DebugLog", false);
        }
        try {
            this.logFileLogger.writeUnsafe(formattedLine, null, false);
        }
        catch (Exception e) {
            this.originalErr.println("Exception thrown writing to log file.");
            this.originalErr.println(e);
            e.printStackTrace(this.originalErr);
        }
    }

    private void echoToRecording(DebugType debugType, LogSeverity logSeverity, String callerAffix, String outString) {
        if (this.recordingOut != null) {
            this.recordingOut.println(this.formatLogStringAnimationRecordingFile(debugType, logSeverity, callerAffix, outString));
        }
    }

    public static void log(DebugType type, String str) {
        type.println(str);
    }

    public static void setLogEnabled(DebugType type, boolean bEnabled) {
        if (type.isEnabled() != bEnabled) {
            type.setLogSeverity(bEnabled ? DebugLog.getDefaultLogSeverity() : LogSeverity.Off);
        }
    }

    public static void log(String str) {
        DebugLog.log(DebugType.General, str);
    }

    @UsedFromLua
    public static ArrayList<DebugType> getDebugTypes() {
        ArrayList<DebugType> debugTypes = new ArrayList<DebugType>(Arrays.asList(DebugType.values()));
        debugTypes.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.name(), b.name()));
        return debugTypes;
    }

    @UsedFromLua
    public static String getSelectedProfileName() {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        return dbgCfgFile.getSelectedProfile();
    }

    @UsedFromLua
    public static List<String> getProfileNames() {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        ArrayList<String> profileNames = new ArrayList<String>();
        for (DebugLogProfile profile : dbgCfgFile.getProfiles()) {
            profileNames.add(profile.getName());
        }
        return profileNames;
    }

    @UsedFromLua
    public static List<String> getProfileAliases() {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        ArrayList<String> aliases = new ArrayList<String>(dbgCfgFile.getAliases());
        aliases.sort(String.CASE_INSENSITIVE_ORDER);
        return aliases;
    }

    @UsedFromLua
    public static LogSeverity getLogSeverityForSelectedProfile(DebugType debugType) {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        String selectedProfile = dbgCfgFile.getSelectedProfile();
        if (selectedProfile.startsWith("$")) {
            LogSeverity logSeverity = null;
            List<DebugLogProfile> profiles = dbgCfgFile.resolveAliasProfiles(selectedProfile);
            for (DebugLogProfile profile : profiles) {
                LogSeverity logSeverity1 = profile.getLogSeverity(debugType);
                if (logSeverity1 == null) continue;
                logSeverity = logSeverity1;
            }
            return logSeverity;
        }
        DebugLogProfile profile = dbgCfgFile.getProfile(selectedProfile);
        return profile.getLogSeverity(debugType);
    }

    @UsedFromLua
    public static void invokeProfile(String profileOrAlias) {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        dbgCfgFile.setSelectedProfile(profileOrAlias);
        if (profileOrAlias.startsWith("$")) {
            DebugLog.log("Selected debug alias = '" + profileOrAlias + "'");
            List<DebugLogProfile> profiles = dbgCfgFile.resolveAliasProfiles(profileOrAlias);
            for (DebugLogProfile profile : profiles) {
                profile.invoke();
            }
            return;
        }
        DebugLog.log("Selected debug profile = '" + profileOrAlias + "'");
        dbgCfgFile.getProfile(profileOrAlias).invoke();
    }

    @UsedFromLua
    public static void invokeSelectedProfile() {
        DebugLog.invokeProfile(DebugLog.getSelectedProfileName());
    }

    @UsedFromLua
    public static void updateSelectedProfileAll(LogSeverity logSeverity) {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        DebugLogProfile profile = dbgCfgFile.getProfile(dbgCfgFile.getSelectedProfile());
        profile.updateAll(logSeverity);
    }

    @UsedFromLua
    public static void updateSelectedProfile(DebugType debugType, LogSeverity logSeverity) {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        DebugLogProfile profile = dbgCfgFile.getProfile(dbgCfgFile.getSelectedProfile());
        profile.update(debugType, logSeverity);
    }

    @UsedFromLua
    public static void writeConfigFile() throws IOException {
        DebugLogCfgFile dbgCfgFile = DebugLog.getInstance().dbgCfgFile;
        if (DebugLog.getInstance().debugCfgFileWatcherPath == null) {
            DebugLog.getInstance().debugCfgFileWatcherPath = DebugLog.getConfigFilePath();
        }
        dbgCfgFile.write(DebugLog.getInstance().debugCfgFileWatcherPath);
    }

    private static String getConfigFilePath() {
        if (GameServer.server) {
            return ZomboidFileSystem.instance.getCacheDirSub("debuglog-server.cfg");
        }
        return ZomboidFileSystem.instance.getCacheDirSub("debuglog.cfg");
    }

    public boolean isLogTraceFileLocationEnabled() {
        return this.logTraceFileLocationEnabled;
    }

    public boolean shouldLogIncludeTimeMs() {
        return this.logTimeMsEnabled;
    }

    public boolean shouldLogIncludeServerTime() {
        return GameServer.server || GameClient.client || this.isLogServerTimeMsEnabled();
    }

    public PrintStream getRecordingOut() {
        return this.recordingOut;
    }

    public void setRecordingOut(PrintStream recordingOut) {
        this.recordingOut = recordingOut;
    }

    public DebugLogStream createLogStream(DebugType debugType) {
        return new DebugLogStream(this.originalOut, this.originalOut, this.originalErr, debugType);
    }

    public boolean isLogServerTimeMsEnabled() {
        return this.logServerTimeMsEnabled;
    }

    public void setLogServerTimeMsEnabled(boolean logServerTimeMsEnabled) {
        this.logServerTimeMsEnabled = logServerTimeMsEnabled;
    }

    public void setStdOut(OutputStream out) {
        this.stdOut.setStream(out);
    }

    public void setStdErr(OutputStream out) {
        this.stdErr.setStream(out);
    }

    public void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.setStdOut(System.out);
        this.setStdErr(System.err);
        System.setOut(DebugType.General.getLogStream());
        System.setErr(this.GeneralErr);
        this.logFileLogger = LoggerManager.getLogger(GameServer.server ? "DebugLog-server" : "DebugLog");
        if (!this.lokiInit) {
            this.lokiInit = true;
            String lokiUrl = System.getProperty("lokiUrl");
            if (lokiUrl != null) {
                System.out.println("Loki logging enabled.");
                String lokiUser = System.getProperty("lokiUser");
                String lokiPass = System.getProperty("lokiPass");
                this.loki = TinyLoki.withUrl(lokiUrl).withThreadExecutor(2000).withBasicAuth(lokiUser, lokiPass).withLabels(Labels.of("instance", GameServer.server ? StatisticManager.getInstanceName() : GameClient.username).l("service_name", GameServer.server ? "pz.server" : "pz.client")).open();
                this.logSet = this.loki.streamSet().open();
            } else {
                this.loki = null;
                this.logSet = null;
            }
        }
        DebugType.General.setLogSeverity(LogSeverity.General);
        DebugType.Lua.setLogSeverity(LogSeverity.General);
        DebugType.Mod.setLogSeverity(LogSeverity.General);
        DebugType.Multiplayer.setLogSeverity(LogSeverity.General);
        DebugType.Network.setLogSeverity(LogSeverity.General);
    }

    public void loadDebugConfig(String filepath) {
        try {
            File file;
            this.dbgCfgFile.setDefault();
            if (!(filepath != null || (file = new File(filepath = DebugLog.getConfigFilePath())).exists() && file.isFile())) {
                return;
            }
            DebugLog.log("Attempting to read debug config...");
            file = new File(filepath);
            if (!file.exists() || !file.isFile()) {
                DebugLog.log("Attempting relative path...");
                File p = new File("");
                Path path = Path.of(p.toURI()).getParent();
                file = new File(String.valueOf(path) + File.separator + filepath);
            }
            DebugType.DetailedInfo.trace("file = " + file.getAbsolutePath());
            if (!file.exists() || !file.isFile()) {
                DebugLog.log("Could not find debug config.");
                return;
            }
            try {
                this.dbgCfgFile.read(filepath);
            }
            catch (Exception e) {
                DebugType.General.printException(e, LogSeverity.Error);
            }
            String selectedProfile = this.dbgCfgFile.getSelectedProfile();
            if (selectedProfile != null) {
                DebugLog.invokeProfile(selectedProfile);
            }
            this.startWatchingDebugCfgFile(file);
        }
        catch (Exception e) {
            DebugType.General.printException(e, LogSeverity.Error);
        }
    }

    private void startWatchingDebugCfgFile(File file) {
        if (this.debugCfgFileWatcher != null && this.debugCfgFileWatcherPath.equalsIgnoreCase(file.getPath())) {
            return;
        }
        if (this.debugCfgFileWatcher != null) {
            this.stopWatchingDebugCfgFile();
        }
        String cfgFileDir = file.getParent();
        DebugFileWatcher.instance.addDirectory(cfgFileDir);
        this.debugCfgFileWatcherPath = file.getPath();
        this.debugCfgFileWatcher = new PredicatedFileWatcher(this.debugCfgFileWatcherPath, this::isDebugCfgPath, this::onDebugCfgFileChanged);
        DebugFileWatcher.instance.add(this.debugCfgFileWatcher);
    }

    private void stopWatchingDebugCfgFile() {
        DebugFileWatcher.instance.remove(this.debugCfgFileWatcher);
        this.debugCfgFileWatcher = null;
        this.debugCfgFileWatcherPath = null;
    }

    private void onDebugCfgFileChanged(String path) {
        this.loadDebugConfig(this.debugCfgFileWatcherPath);
        DebugLog.printLogLevels();
    }

    private boolean isDebugCfgPath(String path) {
        return StringUtils.equalsIgnoreCase(this.debugCfgFileWatcherPath, path);
    }

    public void readConfigCommand(String s, boolean enable) {
        try {
            DebugType type;
            String logTypeStr = s;
            String logSeverityStr = null;
            if (StringUtils.containsWhitespace(s)) {
                String[] split = s.split("\\s+");
                logTypeStr = split[0].trim();
                logSeverityStr = split[1].trim();
            }
            LogSeverity logSeverity = LogSeverity.Debug;
            if (!StringUtils.isNullOrWhitespace(logSeverityStr)) {
                logSeverity = LogSeverity.valueOf(logSeverityStr);
            }
            if (logTypeStr.equalsIgnoreCase("LogTraceFileLocation")) {
                this.logTraceFileLocationEnabled = enable;
                return;
            }
            if (logTypeStr.equalsIgnoreCase("LogTimeMsEnabled")) {
                this.logTimeMsEnabled = enable;
                return;
            }
            if (logTypeStr.equalsIgnoreCase("LogServerTimeMsEnabled")) {
                this.logServerTimeMsEnabled = enable;
                return;
            }
            if (logTypeStr.equalsIgnoreCase("all")) {
                for (DebugType type2 : DebugType.values()) {
                    if (type2 == DebugType.General && !enable) continue;
                    type2.setLogSeverity(logSeverity);
                    DebugLog.setLogEnabled(type2, enable);
                }
                return;
            }
            if (logTypeStr.contains(".")) {
                String[] split = logTypeStr.split("\\.");
                type = DebugType.valueOf(split[0]);
                ScriptType scriptType = ScriptType.valueOf(split[1]);
                ScriptManager.EnableDebug(scriptType, enable);
            } else {
                type = DebugType.valueOf(logTypeStr);
            }
            type.setLogSeverity(logSeverity);
            DebugLog.setLogEnabled(type, enable);
        }
        catch (Exception e) {
            DebugType.General.printException(e, "Exception thrown in readConfigCommand", LogSeverity.Error);
        }
    }

    public static void nativeLog(String logType, String logSeverity, String logTxt) {
        DebugType type = StringUtils.tryParseEnum(DebugType.class, logType, DebugType.General);
        LogSeverity severity = StringUtils.tryParseEnum(LogSeverity.class, logSeverity, LogSeverity.General);
        type.routedWrite(1, severity, logTxt);
    }

    static {
        instanceLock = new Object();
    }

    private static final class OutputStreamWrapper
    extends FilterOutputStream {
        public OutputStreamWrapper(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.out.write(b, off, len);
            if (Core.debug && UIDebugConsole.instance != null && DebugOptions.instance.uiDebugConsoleDebugLog.getValue()) {
                UIDebugConsole.instance.addOutput(b, off, len);
            }
        }

        public void setStream(OutputStream out) {
            this.out = out;
        }
    }
}

