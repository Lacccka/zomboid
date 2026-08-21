/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.PackingOptions;

public class PackingUtils {
    private static PackingLogger packingLogger = new PackingLogger("org.harmony.apache.pack200", null);
    private static FileHandler fileHandler;

    public static void config(PackingOptions options) throws IOException {
        String logFileName;
        String string = logFileName = options != null ? options.getLogFile() : null;
        if (fileHandler != null) {
            fileHandler.close();
        }
        if (logFileName != null) {
            fileHandler = new FileHandler(logFileName, false);
            fileHandler.setFormatter(new SimpleFormatter());
            packingLogger.addHandler(fileHandler);
            packingLogger.setUseParentHandlers(false);
        }
        if (options != null) {
            PackingUtils.packingLogger.setVerbose(options.isVerbose());
        }
    }

    public static void copyThroughJar(JarFile jarFile, OutputStream outputStream2) throws IOException {
        try (JarOutputStream jarOutputStream = new JarOutputStream(outputStream2);){
            jarOutputStream.setComment("PACK200");
            byte[] bytes = new byte[16384];
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                jarOutputStream.putNextEntry(jarEntry);
                InputStream inputStream2 = jarFile.getInputStream(jarEntry);
                try {
                    int bytesRead;
                    while ((bytesRead = inputStream2.read(bytes)) != -1) {
                        jarOutputStream.write(bytes, 0, bytesRead);
                    }
                    jarOutputStream.closeEntry();
                    PackingUtils.log("Packed " + jarEntry.getName());
                }
                finally {
                    if (inputStream2 == null) continue;
                    inputStream2.close();
                }
            }
            jarFile.close();
        }
    }

    public static void copyThroughJar(JarInputStream jarInputStream, OutputStream outputStream2) throws IOException {
        Manifest manifest = jarInputStream.getManifest();
        try (JarOutputStream jarOutputStream = new JarOutputStream(outputStream2, manifest);){
            JarEntry jarEntry;
            jarOutputStream.setComment("PACK200");
            PackingUtils.log("Packed META-INF/MANIFEST.MF");
            byte[] bytes = new byte[16384];
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                int bytesRead;
                jarOutputStream.putNextEntry(jarEntry);
                while ((bytesRead = jarInputStream.read(bytes)) != -1) {
                    jarOutputStream.write(bytes, 0, bytesRead);
                }
                PackingUtils.log("Packed " + jarEntry.getName());
            }
            jarInputStream.close();
        }
    }

    public static List<Archive.PackingFile> getPackingFileListFromJar(JarFile jarFile, boolean keepFileOrder) throws IOException {
        ArrayList<Archive.PackingFile> packingFileList = new ArrayList<Archive.PackingFile>();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            InputStream inputStream2 = jarFile.getInputStream(jarEntry);
            try {
                byte[] bytes = PackingUtils.readJarEntry(jarEntry, new BufferedInputStream(inputStream2));
                packingFileList.add(new Archive.PackingFile(bytes, jarEntry));
            }
            finally {
                if (inputStream2 == null) continue;
                inputStream2.close();
            }
        }
        if (!keepFileOrder) {
            PackingUtils.reorderPackingFiles(packingFileList);
        }
        return packingFileList;
    }

    public static List<Archive.PackingFile> getPackingFileListFromJar(JarInputStream jarInputStream, boolean keepFileOrder) throws IOException {
        JarEntry jarEntry;
        ArrayList<Archive.PackingFile> packingFileList = new ArrayList<Archive.PackingFile>();
        Manifest manifest = jarInputStream.getManifest();
        if (manifest != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manifest.write(baos);
            packingFileList.add(new Archive.PackingFile("META-INF/MANIFEST.MF", baos.toByteArray(), 0L));
        }
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            byte[] bytes = PackingUtils.readJarEntry(jarEntry, new BufferedInputStream(jarInputStream));
            packingFileList.add(new Archive.PackingFile(bytes, jarEntry));
        }
        if (!keepFileOrder) {
            PackingUtils.reorderPackingFiles(packingFileList);
        }
        return packingFileList;
    }

    public static void log(String message) {
        packingLogger.log(Level.INFO, message);
    }

    private static byte[] readJarEntry(JarEntry jarEntry, InputStream inputStream2) throws IOException {
        byte[] bytes;
        long size = jarEntry.getSize();
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Large Class!");
        }
        if (size < 0L) {
            size = 0L;
        }
        if ((long)inputStream2.read(bytes = new byte[(int)size]) != size) {
            throw new IllegalArgumentException("Error reading from stream");
        }
        return bytes;
    }

    private static void reorderPackingFiles(List<Archive.PackingFile> packingFileList) {
        Iterator<Archive.PackingFile> iterator2 = packingFileList.iterator();
        while (iterator2.hasNext()) {
            Archive.PackingFile packingFile = iterator2.next();
            if (!packingFile.isDirectory()) continue;
            iterator2.remove();
        }
        packingFileList.sort((arg0, arg1) -> {
            String fileName1;
            String fileName0 = arg0.getName();
            if (fileName0.equals(fileName1 = arg1.getName())) {
                return 0;
            }
            if ("META-INF/MANIFEST.MF".equals(fileName0)) {
                return -1;
            }
            if ("META-INF/MANIFEST.MF".equals(fileName1)) {
                return 1;
            }
            return fileName0.compareTo(fileName1);
        });
    }

    static {
        LogManager.getLogManager().addLogger(packingLogger);
    }

    private static final class PackingLogger
    extends Logger {
        private boolean verbose;

        protected PackingLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }

        @Override
        public void log(LogRecord logRecord) {
            if (this.verbose) {
                super.log(logRecord);
            }
        }

        private void setVerbose(boolean isVerbose) {
            this.verbose = isVerbose;
        }
    }
}

