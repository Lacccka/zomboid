/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.util.Map;
import java.util.WeakHashMap;
import javax.activation.CommandInfo;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.activation.SecuritySupport;

public abstract class CommandMap {
    private static CommandMap defaultCommandMap = null;
    private static Map<ClassLoader, CommandMap> map = new WeakHashMap<ClassLoader, CommandMap>();

    public static synchronized CommandMap getDefaultCommandMap() {
        if (defaultCommandMap != null) {
            return defaultCommandMap;
        }
        ClassLoader tccl = SecuritySupport.getContextClassLoader();
        CommandMap def = map.get(tccl);
        if (def == null) {
            def = new MailcapCommandMap();
            map.put(tccl, def);
        }
        return def;
    }

    public static synchronized void setDefaultCommandMap(CommandMap commandMap) {
        block3: {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkSetFactory();
                }
                catch (SecurityException ex) {
                    ClassLoader cl = CommandMap.class.getClassLoader();
                    if (cl != null && cl.getParent() != null && cl == commandMap.getClass().getClassLoader()) break block3;
                    throw ex;
                }
            }
        }
        map.remove(SecuritySupport.getContextClassLoader());
        defaultCommandMap = commandMap;
    }

    public abstract CommandInfo[] getPreferredCommands(String var1);

    public CommandInfo[] getPreferredCommands(String mimeType, DataSource ds) {
        return this.getPreferredCommands(mimeType);
    }

    public abstract CommandInfo[] getAllCommands(String var1);

    public CommandInfo[] getAllCommands(String mimeType, DataSource ds) {
        return this.getAllCommands(mimeType);
    }

    public abstract CommandInfo getCommand(String var1, String var2);

    public CommandInfo getCommand(String mimeType, String cmdName, DataSource ds) {
        return this.getCommand(mimeType, cmdName);
    }

    public abstract DataContentHandler createDataContentHandler(String var1);

    public DataContentHandler createDataContentHandler(String mimeType, DataSource ds) {
        return this.createDataContentHandler(mimeType);
    }

    public String[] getMimeTypes() {
        return null;
    }
}

