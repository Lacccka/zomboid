/*
 * Decompiled with CFR 0.152.
 */
package org.luaj.kahluafork.compiler;

final class InstructionPtr {
    final int[] code;
    final int idx;

    InstructionPtr(int[] code, int idx) {
        this.code = code;
        this.idx = idx;
    }

    int get() {
        return this.code[this.idx];
    }

    void set(int value) {
        this.code[this.idx] = value;
    }
}

