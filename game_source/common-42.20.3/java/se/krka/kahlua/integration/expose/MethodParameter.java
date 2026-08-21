/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

public class MethodParameter {
    private final String name;
    private final String type;
    private final String description;

    public MethodParameter(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }
}

