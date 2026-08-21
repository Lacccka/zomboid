/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class CPUTF8
extends ConstantPoolEntry {
    private final String utf8;
    private boolean hashCodeComputed;
    private int cachedHashCode;

    public CPUTF8(String string) {
        this(string, -1);
    }

    public CPUTF8(String utf8, int globalIndex) {
        super((byte)1, globalIndex);
        this.utf8 = Objects.requireNonNull(utf8, "utf8");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CPUTF8 other = (CPUTF8)obj;
        return this.utf8.equals(other.utf8);
    }

    private void generateHashCode() {
        this.hashCodeComputed = true;
        int PRIME = 31;
        this.cachedHashCode = 31 + this.utf8.hashCode();
    }

    @Override
    public int hashCode() {
        if (!this.hashCodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }

    public void setGlobalIndex(int index) {
        this.globalIndex = index;
    }

    @Override
    public String toString() {
        return StandardCharsets.UTF_8.name() + ":" + this.utf8;
    }

    public String underlyingString() {
        return this.utf8;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeUTF(this.utf8);
    }
}

