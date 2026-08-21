/*
 * Decompiled with CFR 0.152.
 */
package zombie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import zombie.ZomboidFileSystem;
import zombie.core.Core;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;

public final class ChunkMapFilenames {
    public static ChunkMapFilenames instance = new ChunkMapFilenames();
    private File dirFile;
    private String cacheDir;
    private final HashSet<Integer> wxFolders = new HashSet();

    public ChunkMapFilenames() {
        File[] directories;
        this.cacheDir = ZomboidFileSystem.instance.getGameModeCacheDir();
        for (File dir : directories = ZomboidFileSystem.listAllDirectories(this.cacheDir + File.separator + Core.gameSaveWorld + File.separator + "map", file -> true, false)) {
            try {
                this.wxFolders.add(Integer.valueOf(dir.getName()));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void clear() {
        this.dirFile = null;
        this.cacheDir = null;
        this.wxFolders.clear();
    }

    public File getFilename(int wx, int wy) {
        if (this.cacheDir == null) {
            this.cacheDir = ZomboidFileSystem.instance.getGameModeCacheDir();
        }
        if (this.wxFolders.add(wx)) {
            try {
                Files.createDirectories(Path.of(this.cacheDir + File.separator + Core.gameSaveWorld + File.separator + "map" + File.separator + wx, new String[0]), new FileAttribute[0]);
            }
            catch (IOException e) {
                DebugType.General.printException(e, "", LogSeverity.Error);
            }
        }
        String filename = this.cacheDir + File.separator + Core.gameSaveWorld + File.separator + "map" + File.separator + wx + File.separator + wy + ".bin";
        return new File(filename);
    }

    public File getDir(String gameSaveWorld) {
        if (this.cacheDir == null) {
            this.cacheDir = ZomboidFileSystem.instance.getGameModeCacheDir();
        }
        if (this.dirFile == null) {
            this.dirFile = new File(this.cacheDir, "map" + File.separator + gameSaveWorld);
        }
        return this.dirFile;
    }

    public String getHeader(int wX, int wY) {
        return wX + "_" + wY + ".lotheader";
    }
}

