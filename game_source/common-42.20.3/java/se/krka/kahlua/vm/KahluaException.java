/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.File;
import zombie.util.list.PZArrayUtil;

public class KahluaException
extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public final Object errorMessage;
    private final String source;
    private final int lineNumber;

    public KahluaException(Object errorMessage) {
        this.errorMessage = errorMessage;
        this.source = null;
        this.lineNumber = -1;
    }

    public KahluaException(Object errorMessage, String source2, int lineNumber) {
        this.errorMessage = errorMessage;
        this.source = source2;
        this.lineNumber = lineNumber;
    }

    @Override
    public String getMessage() {
        if (this.errorMessage == null) {
            return "nil";
        }
        return this.errorMessage.toString();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if (this.source == null) {
            return super.getStackTrace();
        }
        String fileName = new File(this.source).getName();
        StackTraceElement sourceElement = new StackTraceElement("Lua", this.source, fileName, this.lineNumber);
        StackTraceElement[] parentStackTrace = super.getStackTrace();
        return PZArrayUtil.insertAt(parentStackTrace, 0, sourceElement);
    }
}

