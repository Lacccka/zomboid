/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.sqlite.util.LibraryLoaderUtil;
import zombie.core.logger.ExceptionLogger;

public class PZSQLUtils {
    public static void init() {
        String resourcePath = LibraryLoaderUtil.getNativeLibResourcePath();
        String fileName = LibraryLoaderUtil.getNativeLibName();
        if (System.getProperty("os.name").startsWith("Win") && LibraryLoaderUtil.hasNativeLib(resourcePath, fileName)) {
            try (InputStream in2 = LibraryLoaderUtil.class.getResourceAsStream(resourcePath + "/" + fileName);){
                String firstLibraryPath = System.getProperty("java.library.path", "").split(File.pathSeparator)[0];
                File target = new File(firstLibraryPath, fileName);
                Files.copy(in2, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                target.setExecutable(true, false);
            }
            catch (IOException in2) {
                // empty catch block
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            ExceptionLogger.logException(e);
            System.exit(1);
        }
    }

    public static Connection getConnection(String absolutePath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + absolutePath);
    }
}

