/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.exception;

import org.apache.commons.lang3.exception.UncheckedReflectiveOperationException;

public class UncheckedIllegalAccessException
extends UncheckedReflectiveOperationException {
    private static final long serialVersionUID = 1L;

    public UncheckedIllegalAccessException(Throwable cause) {
        super(cause);
    }
}

