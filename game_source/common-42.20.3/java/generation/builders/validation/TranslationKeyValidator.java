/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

public class TranslationKeyValidator {
    public static final Set<String> TRANSLATION_KEYS = new HashSet<String>();
    private final String name;

    public static TranslationKeyValidator of(String name) {
        return TranslationKeyValidator.of("%s", name);
    }

    public static TranslationKeyValidator of(String template, String name) {
        return TranslationKeyValidator.of(List.of(template), name);
    }

    public static TranslationKeyValidator of(List<String> templates, String name) {
        return new TranslationKeyValidator(templates, name);
    }

    private TranslationKeyValidator(List<String> templates, String name) {
        this.name = name;
        if (templates.stream().map(t -> t.formatted(name)).noneMatch(TRANSLATION_KEYS::contains)) {
            System.err.println("Missing translation key: %s".formatted(name));
        }
    }

    public String toString() {
        return this.name;
    }

    static {
        try {
            Files.walk(Paths.get("media/lua/shared/Translate/EN", new String[0]), new FileVisitOption[0]).filter(p -> p.toString().endsWith(".json")).map(p -> {
                try {
                    return new JSONObject(Files.readString(p));
                }
                catch (Exception e) {
                    throw new RuntimeException("Error in file: %s".formatted(p), e);
                }
            }).flatMap(json -> json.keySet().stream()).forEach(TRANSLATION_KEYS::add);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

