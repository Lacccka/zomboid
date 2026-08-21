/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import javax.activation.MimetypesFileTypeMap;
import javax.activation.SecuritySupport;

public abstract class FileTypeMap {
    private static FileTypeMap defaultMap = null;
    private static Map<ClassLoader, FileTypeMap> map = new WeakHashMap<ClassLoader, FileTypeMap>();

    public abstract String getContentType(File var1);

    public abstract String getContentType(String var1);

    public static synchronized void setDefaultFileTypeMap(FileTypeMap fileTypeMap) {
        block3: {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkSetFactory();
                }
                catch (SecurityException ex) {
                    ClassLoader cl = FileTypeMap.class.getClassLoader();
                    if (cl != null && cl.getParent() != null && cl == fileTypeMap.getClass().getClassLoader()) break block3;
                    throw ex;
                }
            }
        }
        map.remove(SecuritySupport.getContextClassLoader());
        defaultMap = fileTypeMap;
    }

    public static synchronized FileTypeMap getDefaultFileTypeMap() {
        if (defaultMap != null) {
            return defaultMap;
        }
        ClassLoader tccl = SecuritySupport.getContextClassLoader();
        FileTypeMap def = map.get(tccl);
        if (def == null) {
            def = new MimetypesFileTypeMap();
            map.put(tccl, def);
        }
        return def;
    }
}

