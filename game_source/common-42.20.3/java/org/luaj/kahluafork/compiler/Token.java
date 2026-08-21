/*
 * Decompiled with CFR 0.152.
 */
package org.luaj.kahluafork.compiler;

public final class Token {
    int token;
    double r;
    String ts;

    public void set(Token other) {
        this.token = other.token;
        this.r = other.r;
        this.ts = other.ts;
    }
}

