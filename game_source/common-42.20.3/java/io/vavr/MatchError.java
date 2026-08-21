/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import java.util.NoSuchElementException;

public class MatchError
extends NoSuchElementException {
    private static final long serialVersionUID = 1L;
    private final Object obj;

    MatchError(Object obj) {
        super(obj == null ? "null" : "type: " + obj.getClass().getName() + ", value: " + obj);
        this.obj = obj;
    }

    public Object getObject() {
        return this.obj;
    }
}

