/*
 * Decompiled with CFR 0.152.
 */
package oshi;

public enum PlatformEnumFFM {
    MACOS("macOS"),
    LINUX("Linux"),
    WINDOWS("Windows"),
    UNSUPPORTED("Unsupported Operating System");

    private final String name;

    private PlatformEnumFFM(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

