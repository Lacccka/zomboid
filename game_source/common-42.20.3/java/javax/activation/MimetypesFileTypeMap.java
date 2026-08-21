/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.activation.registries.LogSupport
 *  com.sun.activation.registries.MimeTypeFile
 */
package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MimeTypeFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import javax.activation.FileTypeMap;
import javax.activation.SecuritySupport;

public class MimetypesFileTypeMap
extends FileTypeMap {
    private MimeTypeFile[] DB;
    private static final int PROG = 0;
    private static final String defaultType = "application/octet-stream";
    private static final String confDir;

    public MimetypesFileTypeMap() {
        Vector<MimeTypeFile> dbv = new Vector<MimeTypeFile>(5);
        MimeTypeFile mf = null;
        dbv.addElement(null);
        LogSupport.log((String)"MimetypesFileTypeMap: load HOME");
        try {
            String path;
            String user_home = System.getProperty("user.home");
            if (user_home != null && (mf = this.loadFile(path = user_home + File.separator + ".mime.types")) != null) {
                dbv.addElement(mf);
            }
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        LogSupport.log((String)"MimetypesFileTypeMap: load SYS");
        try {
            if (confDir != null && (mf = this.loadFile(confDir + "mime.types")) != null) {
                dbv.addElement(mf);
            }
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        LogSupport.log((String)"MimetypesFileTypeMap: load JAR");
        this.loadAllResources(dbv, "META-INF/mime.types");
        LogSupport.log((String)"MimetypesFileTypeMap: load DEF");
        mf = this.loadResource("/META-INF/mimetypes.default");
        if (mf != null) {
            dbv.addElement(mf);
        }
        this.DB = new MimeTypeFile[dbv.size()];
        dbv.copyInto(this.DB);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private MimeTypeFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(this.getClass(), name);
            if (clis != null) {
                MimeTypeFile mf = new MimeTypeFile(clis);
                if (LogSupport.isLoggable()) {
                    LogSupport.log((String)("MimetypesFileTypeMap: successfully loaded mime types file: " + name));
                }
                MimeTypeFile mimeTypeFile = mf;
                return mimeTypeFile;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log((String)("MimetypesFileTypeMap: not loading mime types file: " + name));
            }
        }
        catch (IOException e) {
            if (LogSupport.isLoggable()) {
                LogSupport.log((String)("MimetypesFileTypeMap: can't load " + name), (Throwable)e);
            }
        }
        catch (SecurityException sex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log((String)("MimetypesFileTypeMap: can't load " + name), (Throwable)sex);
            }
        }
        finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            }
            catch (IOException e) {}
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadAllResources(Vector v, String name) {
        boolean anyLoaded;
        block25: {
            anyLoaded = false;
            try {
                URL[] urls2;
                ClassLoader cld = null;
                cld = SecuritySupport.getContextClassLoader();
                if (cld == null) {
                    cld = this.getClass().getClassLoader();
                }
                if ((urls2 = cld != null ? SecuritySupport.getResources(cld, name) : SecuritySupport.getSystemResources(name)) == null) break block25;
                if (LogSupport.isLoggable()) {
                    LogSupport.log((String)"MimetypesFileTypeMap: getResources");
                }
                for (int i = 0; i < urls2.length; ++i) {
                    URL url = urls2[i];
                    InputStream clis = null;
                    if (LogSupport.isLoggable()) {
                        LogSupport.log((String)("MimetypesFileTypeMap: URL " + url));
                    }
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.addElement(new MimeTypeFile(clis));
                            anyLoaded = true;
                            if (!LogSupport.isLoggable()) continue;
                            LogSupport.log((String)("MimetypesFileTypeMap: successfully loaded mime types from URL: " + url));
                            continue;
                        }
                        if (!LogSupport.isLoggable()) continue;
                        LogSupport.log((String)("MimetypesFileTypeMap: not loading mime types from URL: " + url));
                        continue;
                    }
                    catch (IOException ioex) {
                        if (!LogSupport.isLoggable()) continue;
                        LogSupport.log((String)("MimetypesFileTypeMap: can't load " + url), (Throwable)ioex);
                        continue;
                    }
                    catch (SecurityException sex) {
                        if (!LogSupport.isLoggable()) continue;
                        LogSupport.log((String)("MimetypesFileTypeMap: can't load " + url), (Throwable)sex);
                        continue;
                    }
                    finally {
                        try {
                            if (clis != null) {
                                clis.close();
                            }
                        }
                        catch (IOException ioex) {}
                    }
                }
            }
            catch (Exception ex) {
                if (!LogSupport.isLoggable()) break block25;
                LogSupport.log((String)("MimetypesFileTypeMap: can't load " + name), (Throwable)ex);
            }
        }
        if (!anyLoaded) {
            LogSupport.log((String)"MimetypesFileTypeMap: !anyLoaded");
            MimeTypeFile mf = this.loadResource("/" + name);
            if (mf != null) {
                v.addElement(mf);
            }
        }
    }

    private MimeTypeFile loadFile(String name) {
        MimeTypeFile mtf = null;
        try {
            mtf = new MimeTypeFile(name);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return mtf;
    }

    public MimetypesFileTypeMap(String mimeTypeFileName) throws IOException {
        this();
        this.DB[0] = new MimeTypeFile(mimeTypeFileName);
    }

    public MimetypesFileTypeMap(InputStream is) {
        this();
        try {
            this.DB[0] = new MimeTypeFile(is);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public synchronized void addMimeTypes(String mime_types) {
        if (this.DB[0] == null) {
            this.DB[0] = new MimeTypeFile();
        }
        this.DB[0].appendToRegistry(mime_types);
    }

    public String getContentType(File f) {
        return this.getContentType(f.getName());
    }

    public synchronized String getContentType(String filename) {
        int dot_pos = filename.lastIndexOf(".");
        if (dot_pos < 0) {
            return defaultType;
        }
        String file_ext = filename.substring(dot_pos + 1);
        if (file_ext.length() == 0) {
            return defaultType;
        }
        for (int i = 0; i < this.DB.length; ++i) {
            String result;
            if (this.DB[i] == null || (result = this.DB[i].getMIMETypeString(file_ext)) == null) continue;
            return result;
        }
        return defaultType;
    }

    static {
        String dir = null;
        try {
            dir = (String)AccessController.doPrivileged(new PrivilegedAction(){

                public Object run() {
                    String home = System.getProperty("java.home");
                    String newdir = home + File.separator + "conf";
                    File conf = new File(newdir);
                    if (conf.exists()) {
                        return newdir + File.separator;
                    }
                    return home + File.separator + "lib" + File.separator;
                }
            });
        }
        catch (Exception exception) {
            // empty catch block
        }
        confDir = dir;
    }
}

