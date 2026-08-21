/*
 * Decompiled with CFR 0.152.
 */
package zombie.network;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import zombie.UsedFromLua;
import zombie.ZomboidFileSystem;
import zombie.core.logger.ExceptionLogger;
import zombie.network.ServerSettings;
import zombie.scripting.FileName;

@UsedFromLua
public class ServerSettingsManager {
    public static final ServerSettingsManager instance = new ServerSettingsManager();
    protected ArrayList<ServerSettings> settings = new ArrayList();
    protected ArrayList<String> suffixes = new ArrayList();
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final int MAX_PATH_LENGTH = 260;
    private static final int SEPARATOR_LENGTH = 1;
    private static final int LONGEST_SUFFIX_LENGTH = "_spawnregions.lua".length();

    public String getSettingsFolder() {
        return ZomboidFileSystem.instance.getCacheDir() + File.separator + "Server";
    }

    public boolean settingsFolderPathValidLength(String filename) {
        return this.getSettingsFolder().length() + 1 + filename.length() + LONGEST_SUFFIX_LENGTH < 260;
    }

    public boolean settingsFolderNameValidLength(String filename) {
        return filename.length() + LONGEST_SUFFIX_LENGTH < 255;
    }

    public boolean settingsFolderValidLengthChecks(String filename) {
        return this.settingsFolderPathValidLength(filename) && this.settingsFolderNameValidLength(filename);
    }

    public String getNameInSettingsFolder(String name) {
        return this.getSettingsFolder() + File.separator + name;
    }

    public void readAllSettings() {
        this.settings.clear();
        File dir = new File(this.getSettingsFolder());
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>(this){
            final /* synthetic */ ServerSettingsManager this$0;
            {
                ServerSettingsManager serverSettingsManager = this$0;
                Objects.requireNonNull(serverSettingsManager);
                this.this$0 = serverSettingsManager;
            }

            @Override
            public boolean accept(Path entry) throws IOException {
                String fileName = entry.getFileName().toString();
                return !Files.isDirectory(entry, new LinkOption[0]) && fileName.endsWith(".ini") && !fileName.endsWith("_zombies.ini") && this.this$0.isValidName(fileName.replace(".ini", ""));
            }
        };
        try (DirectoryStream<Path> dstrm = Files.newDirectoryStream(dir.toPath(), (DirectoryStream.Filter<? super Path>)filter);){
            for (Path path : dstrm) {
                ServerSettings settings = new ServerSettings(path.getFileName().toString().replace(".ini", ""));
                this.settings.add(settings);
            }
        }
        catch (Exception ex) {
            ExceptionLogger.logException(ex);
        }
    }

    public int getSettingsCount() {
        return this.settings.size();
    }

    public ServerSettings getSettingsByIndex(int index) {
        if (index >= 0 && index < this.settings.size()) {
            return this.settings.get(index);
        }
        return null;
    }

    public boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (name.contains("/") || name.contains("\\") || name.contains(":") || name.contains(";") || name.contains("\"") || name.contains(".")) {
            return false;
        }
        return !name.contains("_zombies");
    }

    private boolean anyFilesExist(String name) {
        this.getSuffixes();
        for (int i = 0; i < this.suffixes.size(); ++i) {
            File file = new File(this.getSettingsFolder() + File.separator + name + this.suffixes.get(i));
            if (!file.exists()) continue;
            return true;
        }
        return false;
    }

    public boolean isValidNewName(String newName) {
        String sanitized = FileName.sanitize(newName);
        if (!this.isValidName(sanitized)) {
            return false;
        }
        return !this.anyFilesExist(sanitized);
    }

    public ArrayList<String> getSuffixes() {
        if (this.suffixes.isEmpty()) {
            this.suffixes.add(".ini");
            this.suffixes.add("_SandboxVars.lua");
            this.suffixes.add("_spawnpoints.lua");
            this.suffixes.add("_spawnregions.lua");
            this.suffixes.add("_zombies.ini");
        }
        return this.suffixes;
    }
}

