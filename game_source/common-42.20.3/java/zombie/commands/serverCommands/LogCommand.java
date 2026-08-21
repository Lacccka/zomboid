/*
 * Decompiled with CFR 0.152.
 */
package zombie.commands.serverCommands;

import java.util.ArrayList;
import java.util.Arrays;
import zombie.characters.Capability;
import zombie.characters.Role;
import zombie.commands.CommandArgs;
import zombie.commands.CommandBase;
import zombie.commands.CommandHelp;
import zombie.commands.CommandName;
import zombie.commands.RequiredCapability;
import zombie.core.Translator;
import zombie.core.logger.ExceptionLogger;
import zombie.core.raknet.UdpConnection;
import zombie.core.znet.ZNet;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.network.PacketTypes;

@CommandName(name="log")
@CommandArgs(required={"(.+)", "(.+)"})
@CommandHelp(helpText="UI_ServerOptionDesc_SetLogLevel")
@RequiredCapability(requiredCapability=Capability.DebugConsole)
public class LogCommand
extends CommandBase {
    private static final String CMD_ALL = "all";
    private static final String CMD_SAVE = "save";

    public LogCommand(String username, Role userRole, String command, UdpConnection connection) {
        super(username, userRole, command, connection);
    }

    public static DebugType getDebugType(String debugType) {
        ArrayList<DebugType> types = new ArrayList<DebugType>();
        for (DebugType type : DebugType.values()) {
            if (type.name().equalsIgnoreCase(debugType)) {
                return type;
            }
            if (!type.name().toLowerCase().startsWith(debugType.toLowerCase())) continue;
            types.add(type);
        }
        return types.size() == 1 ? (DebugType)((Object)types.get(0)) : null;
    }

    public static LogSeverity getLogSeverity(String logSeverity) {
        ArrayList<LogSeverity> severities = new ArrayList<LogSeverity>();
        for (LogSeverity severity : LogSeverity.values()) {
            if (!severity.name().toLowerCase().startsWith(logSeverity.toLowerCase())) continue;
            severities.add(severity);
        }
        return severities.size() == 1 ? (LogSeverity)((Object)severities.get(0)) : null;
    }

    public static String process(String arg1, String arg2) {
        DebugType type = LogCommand.getDebugType(arg1);
        LogSeverity severity = LogCommand.getLogSeverity(arg2);
        if (type != null && severity != null) {
            type.setLogSeverity(severity);
            if (DebugType.ZNet == type) {
                ZNet.setLogLevel(severity);
            }
            DebugLog.updateSelectedProfile(type, severity);
            return String.format("Debug type \"%s\" log level is set to \"%s\"", type.name().toLowerCase(), severity.name().toLowerCase());
        }
        if (CMD_ALL.equals(arg1) && severity != null) {
            for (DebugType debugType : DebugType.values()) {
                debugType.setLogSeverity(severity);
            }
            DebugLog.updateSelectedProfileAll(severity);
            return String.format("All debug type log levels are set to \"%s\"", severity.name().toLowerCase());
        }
        if (CMD_SAVE.equals(arg1) && CMD_ALL.equals(arg2)) {
            try {
                DebugLog.writeConfigFile();
                return "DebugLog save succeeded";
            }
            catch (Exception e) {
                ExceptionLogger.logException(e);
                return "DebugLog save failed";
            }
        }
        if (DebugType.Packet == type) {
            if (CMD_ALL.equals(arg2)) {
                for (PacketTypes.PacketType packetType : PacketTypes.PacketType.values()) {
                    packetType.setLogEnabled(true);
                }
                return "All packet types logging is enabled";
            }
            if ("none".equals(arg2)) {
                for (PacketTypes.PacketType packetType : PacketTypes.PacketType.values()) {
                    packetType.setLogEnabled(false);
                }
                return "All packet types logging is disabled";
            }
            PacketTypes.PacketType packetType = Arrays.stream(PacketTypes.PacketType.values()).filter(packet -> packet.name().equalsIgnoreCase(arg2)).findFirst().orElse(null);
            if (packetType != null) {
                packetType.setLogEnabled(!packetType.isLogEnabled());
                return String.format("Packet type \"%s\" logging is \"%s\"", packetType.name(), packetType.isLogEnabled() ? "enabled" : "disabled");
            }
        }
        return Translator.getText("UI_ServerOptionDesc_SetLogLevel", type == null ? "\"packet type\"" : type.name().toLowerCase(), severity == null ? "\"log severity\"" : severity.name().toLowerCase());
    }

    @Override
    protected String Command() {
        return LogCommand.process(this.getCommandArg(0), this.getCommandArg(1));
    }
}

