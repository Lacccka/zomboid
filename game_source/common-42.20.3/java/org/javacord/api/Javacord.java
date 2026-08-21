/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

public class Javacord {
    public static final String VERSION;
    public static final String COMMIT_ID;
    public static final Instant BUILD_TIMESTAMP;
    public static final String DISCORD_DOMAIN = "discord.com";
    public static final String DISCORD_CDN_DOMAIN = "cdn.discordapp.com";
    public static final String DISPLAY_VERSION;
    public static final String GITHUB_URL = "https://github.com/Javacord/Javacord";
    public static final String USER_AGENT;
    public static final String DISCORD_GATEWAY_VERSION = "10";
    public static final String DISCORD_VOICE_GATEWAY_VERSION = "4";
    public static final String DISCORD_API_VERSION = "10";

    private Javacord() {
        throw new UnsupportedOperationException();
    }

    static {
        Properties versionProperties = new Properties();
        try (InputStream versionPropertiesStream2 = Javacord.class.getResourceAsStream("/git.properties");){
            versionProperties.load(versionPropertiesStream2);
        }
        catch (IOException versionPropertiesStream2) {
            // empty catch block
        }
        VERSION = versionProperties.getProperty("version", "<unknown>");
        COMMIT_ID = versionProperties.getProperty("git.commit.id.abbrev", "<unknown>");
        String buildTimestamp = versionProperties.getProperty("buildTimestamp", null);
        BUILD_TIMESTAMP = buildTimestamp == null ? null : Instant.parse(buildTimestamp);
        DISPLAY_VERSION = VERSION.endsWith("-SNAPSHOT") ? String.format("%s [%s]", VERSION, BUILD_TIMESTAMP) : VERSION;
        USER_AGENT = "DiscordBot (https://github.com/Javacord/Javacord, v" + DISPLAY_VERSION + ")";
    }
}

