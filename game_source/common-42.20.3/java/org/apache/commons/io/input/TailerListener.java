/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import org.apache.commons.io.input.Tailer;

public interface TailerListener {
    public void fileNotFound();

    public void fileRotated();

    public void handle(Exception var1);

    public void handle(String var1);

    public void init(Tailer var1);
}

