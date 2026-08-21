/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DiscordLocale {
    DANISH("da"),
    GERMAN("de"),
    ENGLISH_UK("en-GB"),
    ENGLISH_US("en-US"),
    SPANISH("es-ES"),
    FRENCH("fr"),
    CROATIAN("hr"),
    ITALIAN("it"),
    LITHUANIAN("lt"),
    HUNGARIAN("hu"),
    DUTCH("nl"),
    NORWEGIAN("no"),
    POLISH("pl"),
    PORTUGUESE_BRAZILIAN("pt-BR"),
    ROMANIAN_ROMANIA("ro"),
    FINNISH("fi"),
    SWEDISH("sv-SE"),
    VIETNAMESE("vi"),
    TURKISH("tr"),
    CZECH("cs"),
    GREEK("el"),
    BULGARIAN("bg"),
    RUSSIAN("ru"),
    UKRAINIAN("uk"),
    HINDI("hi"),
    THAI("th"),
    CHINESE_CHINA("zh-CN"),
    JAPANESE("ja"),
    CHINESE_TAIWAN("zh-TW"),
    KOREAN("ko"),
    UNKNOWN("");

    private static final Map<String, DiscordLocale> instanceByCode;
    private final String localeCode;

    private DiscordLocale(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getLocaleCode() {
        return this.localeCode;
    }

    public static DiscordLocale fromLocaleCode(String localeCode) {
        return instanceByCode.getOrDefault(localeCode, UNKNOWN);
    }

    static {
        instanceByCode = Collections.unmodifiableMap(Arrays.stream(DiscordLocale.values()).collect(Collectors.toMap(DiscordLocale::getLocaleCode, Function.identity())));
    }
}

