/*
 * Decompiled with CFR 0.152.
 */
package zombie.debug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.util.StringUtils;

public final class DebugLogProfile {
    private final String name;
    private final List<String> commands = new ArrayList<String>();

    public DebugLogProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public DebugLogProfile addCommand(String command) {
        this.commands.add(command);
        return this;
    }

    public String getCommandArgument0(String command) {
        String trimmed = command.trim();
        if (StringUtils.containsWhitespace(trimmed)) {
            String[] split = trimmed.split("\\s+");
            return split[0];
        }
        return trimmed;
    }

    public void removeCommandsWithArgument0(String argument) {
        for (int i = 0; i < this.commands.size(); ++i) {
            String argument0 = this.getCommandArgument0(this.commands.get(i));
            if (!argument0.equals(argument)) continue;
            this.commands.remove(i--);
        }
    }

    public void removeDebugTypeCommands() {
        HashSet<String> debugTypeCommands = new HashSet<String>();
        debugTypeCommands.add("+all");
        debugTypeCommands.add("-all");
        for (DebugType debugType : DebugType.values()) {
            debugTypeCommands.add("+%s".formatted(debugType.name()));
            debugTypeCommands.add("-%s".formatted(debugType.name()));
        }
        for (int i = 0; i < this.commands.size(); ++i) {
            String argument0 = this.getCommandArgument0(this.commands.get(i));
            if (!debugTypeCommands.contains(argument0)) continue;
            this.commands.remove(i--);
        }
    }

    public void invoke() {
        for (String s : this.commands) {
            if (s.startsWith("+")) {
                DebugLog.getInstance().readConfigCommand(s.substring(1), true);
                continue;
            }
            if (s.startsWith("-")) {
                DebugLog.getInstance().readConfigCommand(s.substring(1), false);
                continue;
            }
            DebugLog.log("unknown command: '" + s + "'");
        }
    }

    public void updateAll(LogSeverity logSeverity) {
        this.removeDebugTypeCommands();
        if (logSeverity == LogSeverity.Off) {
            this.addCommand("-all");
        } else if (logSeverity != null) {
            this.addCommand("+all %s".formatted(new Object[]{logSeverity}));
        }
    }

    public void update(DebugType debugType, LogSeverity logSeverity) {
        String commandEnable = "+%s".formatted(debugType.name());
        String commandDisable = "-%s".formatted(debugType.name());
        this.removeCommandsWithArgument0(commandEnable);
        this.removeCommandsWithArgument0(commandDisable);
        if (logSeverity == LogSeverity.Off) {
            this.addCommand(commandDisable);
        } else if (logSeverity != null) {
            this.addCommand("+%s %s".formatted(debugType.name(), logSeverity.name()));
        }
    }

    public LogSeverity getLogSeverity(DebugType debugType) {
        String commandEnable = "+%s".formatted(debugType.name());
        String commandDisable = "-%s".formatted(debugType.name());
        LogSeverity logSeverity = null;
        for (int i = 0; i < this.commands.size(); ++i) {
            String argument0 = this.getCommandArgument0(this.commands.get(i));
            if ("-all".equalsIgnoreCase(argument0) || commandDisable.equals(argument0)) {
                logSeverity = LogSeverity.Off;
                continue;
            }
            if (!"+all".equals(argument0) && !commandEnable.equals(argument0)) continue;
            logSeverity = this.parseLogSeverity(this.commands.get(i).trim());
        }
        return logSeverity;
    }

    private LogSeverity parseLogSeverity(String command) {
        String logSeverityStr = null;
        if (StringUtils.containsWhitespace(command)) {
            String[] split = command.split("\\s+");
            logSeverityStr = split[1].trim();
        }
        return StringUtils.tryParseEnum(LogSeverity.class, logSeverityStr, LogSeverity.Debug);
    }

    public void write(BufferedWriter bw) throws IOException {
        bw.write(this.getName());
        bw.newLine();
        bw.write(123);
        bw.newLine();
        for (String s : this.commands) {
            bw.write("    ");
            bw.write(s);
            bw.newLine();
        }
        bw.write(125);
        bw.newLine();
    }
}

