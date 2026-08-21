/*
 * Decompiled with CFR 0.152.
 */
package zombie.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONObject;
import zombie.util.StringUtils;

public final class LanguageFile {
    private static final int VERSION1 = 1;
    private static final int VERSION = 1;
    public static final String VERSION_JSON_VALUE = "version";
    public static final String LANGUAGE_NAME_JSON_VALUE = "language_name";
    public static final String BASE_JSON_VALUE = "base";
    public static final String AZERTY_JSON_VALUE = "azerty";
    public String name;
    public String language_name;
    public String base;
    public boolean azerty;

    public LanguageFile(String name, Path path) throws IOException {
        this.name = name;
        JSONObject language_json = new JSONObject(Files.readString(path));
        int version = language_json.optInt(VERSION_JSON_VALUE, 1);
        this.language_name = language_json.optString(LANGUAGE_NAME_JSON_VALUE, null);
        this.base = language_json.optString(BASE_JSON_VALUE, null);
        this.azerty = language_json.optBoolean(AZERTY_JSON_VALUE, false);
        if (version < 1 || version > 1) {
            throw new RuntimeException("invalid or missing version");
        }
        if (StringUtils.isNullOrWhitespace(this.language_name)) {
            throw new RuntimeException("missing or empty value \"language_name\" for language " + name);
        }
    }
}

