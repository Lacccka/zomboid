/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

public enum MessageDecoration {
    ITALICS("*"),
    BOLD("**"),
    STRIKEOUT("~~"),
    CODE_SIMPLE("`"),
    CODE_LONG("```"),
    UNDERLINE("__"),
    SPOILER("||");

    private final String prefix;
    private final String suffix;

    private MessageDecoration(String prefix) {
        this.prefix = prefix;
        this.suffix = new StringBuilder(prefix).reverse().toString();
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public String applyToText(String text) {
        return this.prefix + text + this.suffix;
    }
}

