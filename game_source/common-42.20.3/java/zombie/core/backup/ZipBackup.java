/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.backup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.jspecify.annotations.NonNull;
import zombie.ZomboidFileSystem;
import zombie.core.Core;
import zombie.core.ThreadGroups;
import zombie.core.logger.ExceptionLogger;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.iso.IsoChunk;
import zombie.network.CoopSlave;
import zombie.network.GameServer;
import zombie.network.ServerOptions;
import zombie.savefile.ServerPlayerDB;

public class ZipBackup {
    private static final int compressionMethod = 0;
    static ParallelScatterZipCreator scatterZipCreator;
    private static long lastBackupTime;
    private static Thread backupThread;

    public static void onStartup() {
        lastBackupTime = System.currentTimeMillis();
        if (ServerOptions.getInstance().backupsOnStart.getValue()) {
            ZipBackup.makeBackupFile(GameServer.serverName, BackupTypes.startup);
        }
    }

    public static void onVersion() {
        if (!ServerOptions.getInstance().backupsOnVersionChange.getValue()) {
            return;
        }
        String serverVersionFile = ZomboidFileSystem.instance.getCacheDir() + File.separator + "backups" + File.separator + "last_server_version.txt";
        String serverVersion = ZipBackup.getStringFromZip(serverVersionFile);
        String gameVersion = Core.getInstance().getGameVersion().toString();
        if (!gameVersion.equals(serverVersion)) {
            ZipBackup.putTextFile(serverVersionFile, gameVersion);
            ZipBackup.makeBackupFile(GameServer.serverName, BackupTypes.version);
        }
    }

    public static void onPeriod() {
        int period = ServerOptions.getInstance().backupsPeriod.getValue();
        if (period <= 0) {
            return;
        }
        if (!(System.currentTimeMillis() - lastBackupTime <= (long)period * 60000L || backupThread != null && backupThread.isAlive())) {
            lastBackupTime = System.currentTimeMillis();
            backupThread = new Thread(ThreadGroups.Workers, new Runnable(){

                @Override
                public void run() {
                    try {
                        this.runInner();
                    }
                    catch (Throwable t) {
                        ExceptionLogger.logException(t);
                    }
                }

                private void runInner() {
                    ServerPlayerDB.getInstance().saveFinishWait();
                    ZipBackup.makeBackupFile(GameServer.serverName, BackupTypes.period);
                }
            });
            backupThread.setDaemon(true);
            backupThread.start();
        }
    }

    public static void waitFinish() {
        if (backupThread == null) {
            return;
        }
        while (backupThread.isAlive()) {
            try {
                Thread.sleep(500L);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isRunning() {
        return backupThread != null && backupThread.isAlive();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void makeBackupFile(String serverName, BackupTypes type) {
        long startTime;
        block12: {
            String backupDir = ZomboidFileSystem.instance.getCacheDir() + File.separator + "backups" + File.separator + type.name();
            startTime = System.currentTimeMillis();
            DebugType.DetailedInfo.trace("Start making backup to: " + backupDir);
            scatterZipCreator = new ParallelScatterZipCreator();
            CoopSlave.status("UI_ServerStatus_CreateBackup");
            OutputStream outputStream2 = null;
            try {
                File backupsDir = new File(backupDir);
                if (!backupsDir.exists()) {
                    backupsDir.mkdirs();
                }
                ZipBackup.rotateBackupFile(type);
                String zipname = backupDir + File.separator + "backup_1.zip";
                try {
                    Files.deleteIfExists(Paths.get(zipname, new String[0]));
                }
                catch (IOException e) {
                    DebugType.General.printException(e, LogSeverity.Error);
                }
                File zipFile = new File(zipname);
                zipFile.delete();
                outputStream2 = new FileOutputStream(zipFile);
                ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream2);
                zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
                zipArchiveOutputStream.setMethod(0);
                zipArchiveOutputStream.setLevel(0);
                ZipBackup.zipTextFile("readme.txt", ZipBackup.getBackupReadme(serverName));
                zipArchiveOutputStream.setComment(ZipBackup.getBackupReadme(serverName));
                ZipBackup.zipFile("options.ini", "options.ini");
                ZipBackup.zipFile("popman-options.ini", "popman-options.ini");
                ZipBackup.zipFile("latestSave.ini", "latestSave.ini");
                ZipBackup.zipFile("debug-options.ini", "debug-options.ini");
                ZipBackup.zipFile("sounds.ini", "sounds.ini");
                ZipBackup.zipFile("gamepadBinding.config", "gamepadBinding.config");
                ZipBackup.zipDir("mods", "mods");
                ZipBackup.zipDir("Lua", "Lua");
                ZipBackup.zipDir("db", "db");
                ZipBackup.zipDir("Server", "Server");
                Object object = IsoChunk.WriteLock;
                synchronized (object) {
                    String saveDir = "Saves" + File.separator + "Multiplayer" + File.separator + serverName;
                    ZipBackup.zipDir(saveDir, saveDir, path -> !path.endsWith("blam"));
                    try {
                        scatterZipCreator.writeTo(zipArchiveOutputStream);
                        DebugLog.log(scatterZipCreator.getStatisticsMessage().toString());
                        zipArchiveOutputStream.close();
                        outputStream2.close();
                    }
                    catch (IOException e) {
                        DebugType.General.printException(e, LogSeverity.Error);
                    }
                }
            }
            catch (Exception e) {
                DebugType.General.printException(e, LogSeverity.Error);
                if (outputStream2 == null) break block12;
                try {
                    outputStream2.close();
                }
                catch (IOException ex) {
                    DebugType.General.printException(ex, LogSeverity.Error);
                }
            }
        }
        DebugLog.log("Backup made in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private static void rotateBackupFile(BackupTypes type) {
        int countBackupFiles = ServerOptions.getInstance().backupsCount.getValue() - 1;
        if (countBackupFiles <= 0) {
            return;
        }
        Path oldestURI = Paths.get(ZomboidFileSystem.instance.getCacheDir() + File.separator + "backups" + File.separator + String.valueOf((Object)type) + File.separator + "backup_" + (countBackupFiles + 1) + ".zip", new String[0]);
        try {
            Files.deleteIfExists(oldestURI);
        }
        catch (IOException e) {
            DebugType.General.printException(e, LogSeverity.Error);
        }
        for (int i = countBackupFiles; i > 0; --i) {
            Path sourceURI = Paths.get(ZomboidFileSystem.instance.getCacheDir() + File.separator + "backups" + File.separator + String.valueOf((Object)type) + File.separator + "backup_" + i + ".zip", new String[0]);
            Path destinationURI = Paths.get(ZomboidFileSystem.instance.getCacheDir() + File.separator + "backups" + File.separator + String.valueOf((Object)type) + File.separator + "backup_" + (i + 1) + ".zip", new String[0]);
            try {
                Files.move(sourceURI, destinationURI, new CopyOption[0]);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static String getBackupReadme(String serverName) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        formatter.format(date);
        int saveWorldVersion = ZipBackup.getWorldVersion(serverName);
        String saveWorldVersionDescription = saveWorldVersion == -2 ? "World isn't exist" : (saveWorldVersion == -1 ? "World version cannot be determined" : String.valueOf(saveWorldVersion));
        return "Backup time: " + formatter.format(date) + "\nServerName: " + serverName + "\nCurrent server version:" + String.valueOf(Core.getInstance().getGameVersion()) + "\nCurrent world version:249\nWorld version in this backup is:" + saveWorldVersionDescription;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static int getWorldVersion(String serverName) {
        File inFile = new File(ZomboidFileSystem.instance.getSaveDir() + File.separator + "Multiplayer" + File.separator + serverName + File.separator + "map_t.bin");
        if (inFile.exists()) {
            try (FileInputStream inStream = new FileInputStream(inFile);){
                DataInputStream input;
                block15: {
                    int n;
                    input = new DataInputStream(inStream);
                    try {
                        byte b1 = input.readByte();
                        byte b2 = input.readByte();
                        byte b3 = input.readByte();
                        byte b4 = input.readByte();
                        if (b1 != 71 || b2 != 77 || b3 != 84 || b4 != 77) break block15;
                        n = input.readInt();
                    }
                    catch (Throwable throwable) {
                        try {
                            input.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    input.close();
                    return n;
                }
                int n = -1;
                input.close();
                return n;
            }
            catch (Exception ex) {
                DebugType.General.printException(ex, LogSeverity.Error);
            }
        }
        return -2;
    }

    private static void putTextFile(String filename, String text) {
        try {
            Path pathFile = Paths.get(filename, new String[0]);
            Files.createDirectories(pathFile.getParent(), new FileAttribute[0]);
            try {
                Files.delete(pathFile);
            }
            catch (Exception exception) {
                // empty catch block
            }
            Files.write(pathFile, text.getBytes(), new OpenOption[0]);
        }
        catch (Exception e) {
            DebugType.General.printException(e, LogSeverity.Error);
        }
    }

    private static String getStringFromZip(String filename) {
        String content = null;
        try {
            Path pathFile = Paths.get(filename, new String[0]);
            if (Files.exists(pathFile, new LinkOption[0])) {
                List<String> lines = Files.readAllLines(pathFile);
                content = lines.get(0);
            }
        }
        catch (Exception e) {
            DebugType.General.printException(e, LogSeverity.Error);
        }
        return content;
    }

    private static void zipTextFile(String filename, String text) {
        InputStreamSupplier streamSupplier = () -> new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(filename);
        zipArchiveEntry.setMethod(0);
        scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
    }

    private static void zipFile(String filename, String infile) {
        Path pathFile = Paths.get(ZomboidFileSystem.instance.getCacheDir() + File.separator + infile, new String[0]);
        if (!Files.exists(pathFile, new LinkOption[0])) {
            return;
        }
        InputStreamSupplier streamSupplier = () -> {
            InputStream is = null;
            try {
                is = Files.newInputStream(pathFile, new OpenOption[0]);
            }
            catch (IOException e) {
                DebugType.General.printException(e, LogSeverity.Error);
            }
            return is;
        };
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(filename);
        zipArchiveEntry.setMethod(0);
        scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
    }

    private static void zipDir(String filename, String indir) {
        ZipBackup.zipDir(filename, indir, path -> false);
    }

    private static void zipDir(final String filename, String indir, final Predicate<Path> filter) {
        final Path pathDir = Paths.get(ZomboidFileSystem.instance.getCacheDirSub(indir), new String[0]);
        if (!Files.exists(pathDir, new LinkOption[0]) || !Files.isDirectory(pathDir, new LinkOption[0])) {
            return;
        }
        try {
            Files.walkFileTree(pathDir, (FileVisitor<? super Path>)new FileVisitor<Path>(){

                @Override
                public @NonNull FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(pathDir) && filter != null && !filter.test(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NonNull FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                    Path relativePath = pathDir.relativize(file);
                    InputStreamSupplier streamSupplier = () -> {
                        InputStream is = null;
                        try {
                            is = Files.newInputStream(file, new OpenOption[0]);
                        }
                        catch (IOException e) {
                            DebugType.General.printException(e, LogSeverity.Error);
                        }
                        return is;
                    };
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(filename + File.separator + relativePath.toString().replace("/", File.separator));
                    zipArchiveEntry.setMethod(0);
                    scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NonNull FileVisitResult visitFileFailed(Path file, @NonNull IOException exc) throws IOException {
                    DebugType.General.printException(exc, LogSeverity.Error);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NonNull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        DebugType.General.printException(exc, LogSeverity.Error);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (Exception e) {
            DebugType.General.printException(e, LogSeverity.Error);
        }
    }

    private static enum BackupTypes {
        period,
        startup,
        version;

    }
}

