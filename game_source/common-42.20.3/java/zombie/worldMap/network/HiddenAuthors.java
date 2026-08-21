/*
 * Decompiled with CFR 0.152.
 */
package zombie.worldMap.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import zombie.ZomboidFileSystem;
import zombie.core.logger.ExceptionLogger;
import zombie.network.GameClient;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;

public final class HiddenAuthors {
    private static final String FILE_NAME = "hidden_authors.ini";
    private static final HashMap<String, UserData> userData = new HashMap();
    private static final Set<String> localHidden = new HashSet<String>();

    public static void clearLocal() {
        localHidden.clear();
    }

    public static void read() {
        userData.clear();
        File file = ZomboidFileSystem.instance.getFileInCurrentSave(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader input = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));){
            String curUserName;
            while ((curUserName = input.readLine()) != null) {
                String[] userNamesArray;
                if (curUserName.isEmpty()) {
                    break;
                }
                String userNames = input.readLine();
                if (userNames == null) {
                    break;
                }
                UserData userData1 = new UserData();
                userData1.userName = curUserName;
                for (String userName : userNamesArray = userNames.split(",")) {
                    String userName1 = userName.replaceAll("&comma;", ",");
                    if (userName1.isEmpty()) continue;
                    userData1.hidden.add(userName1);
                }
                userData.put(curUserName, userData1);
            }
        }
        catch (IOException ex) {
            ExceptionLogger.logException(ex);
        }
    }

    public static void write() {
        String fileName = ZomboidFileSystem.instance.getFileNameInCurrentSave(FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(fileName, false);
             OutputStreamWriter osw = new OutputStreamWriter((OutputStream)fos, StandardCharsets.UTF_8);){
            for (Map.Entry<String, UserData> it : userData.entrySet()) {
                if (it.getValue().hidden.isEmpty()) continue;
                osw.write(it.getKey());
                osw.append(System.lineSeparator());
                StringBuilder sb = new StringBuilder();
                for (String userName : it.getValue().hidden) {
                    String userName2 = userName.replaceAll(",", "&comma;");
                    sb.append(userName2);
                    sb.append(",");
                }
                osw.write(sb.toString());
                osw.append(System.lineSeparator());
            }
        }
        catch (IOException ex) {
            ExceptionLogger.logException(ex);
        }
    }

    public static void serverSetAuthorHidden(String userName, String authorName, boolean hidden) {
        UserData userData1 = userData.computeIfAbsent(userName, k -> new UserData());
        if (hidden) {
            userData1.hidden.add(authorName);
        } else {
            userData1.hidden.remove(authorName);
        }
    }

    public static void clientSetAuthorHidden(String authorName, boolean hidden) {
        if (hidden) {
            localHidden.add(authorName);
        } else {
            localHidden.remove(authorName);
        }
    }

    public static void setAuthorHidden(String authorName, boolean hidden) {
        HiddenAuthors.clientSetAuthorHidden(authorName, hidden);
        if (GameClient.client) {
            INetworkPacket.send(PacketTypes.PacketType.HiddenAuthors, hidden, authorName);
        }
    }

    public static boolean isAuthorHidden(String authorName) {
        return localHidden.contains(authorName);
    }

    public static Set<String> getSetForUser(String userName) {
        UserData userData1 = userData.get(userName);
        if (userData1 != null) {
            return userData1.hidden;
        }
        return null;
    }

    private static final class UserData {
        String userName;
        final Set<String> hidden = new HashSet<String>();

        private UserData() {
        }
    }
}

